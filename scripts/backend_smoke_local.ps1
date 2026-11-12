param(
    [int]$Port = 8091,
    [string]$Profile = "local",
    [switch]$SkipBuild,
    [string]$Username = $env:COSTANORTE_DEMO_USER_USERNAME,
    [string]$Password = $env:COSTANORTE_DEMO_USER_PASSWORD,
    [string]$SupervisorUsername = $env:COSTANORTE_BOOTSTRAP_SUPERVISOR_USERNAME,
    [string]$SupervisorPassword = $env:COSTANORTE_BOOTSTRAP_SUPERVISOR_PASSWORD,
    [string]$ProviderUsername = "smoke.provider.maintenance",
    [string]$ProviderPassword = "smoke123"
)

$ErrorActionPreference = "Stop"

function Get-HttpStatusCode {
    param([System.Management.Automation.ErrorRecord]$ErrorRecord)

    if ($null -eq $ErrorRecord.Exception.Response) {
        return $null
    }
    return [int]$ErrorRecord.Exception.Response.StatusCode
}

function Select-ProviderResource {
    param(
        [object[]]$Users,
        [string]$ProviderType,
        [string]$ProviderUsername,
        [object[]]$Resources
    )

    $existingUser = @($Users) | Where-Object { $_.username -eq $ProviderUsername } | Select-Object -First 1
    if ($null -ne $existingUser) {
        $existingResource = @($Resources) | Where-Object {
            $_.id -eq $existingUser.providerAccess.resourceId
        } | Select-Object -First 1
        if ($null -eq $existingResource) {
            throw "El recurso $ProviderType de $ProviderUsername ya no esta activo o disponible"
        }
        return $existingResource
    }

    $assignedIds = @($Users) | Where-Object {
        $_.role -eq "PROVIDER" -and $_.providerAccess.type -eq $ProviderType
    } | ForEach-Object { $_.providerAccess.resourceId }
    $availableResource = @($Resources) | Where-Object {
        $_.id -notin $assignedIds
    } | Select-Object -First 1
    if ($null -eq $availableResource) {
        throw "No hay un recurso $ProviderType activo sin cuenta para el smoke"
    }
    return $availableResource
}

function Set-SmokeProviderUser {
    param(
        [string]$BaseUrl,
        [hashtable]$SupervisorHeaders,
        [object[]]$Users,
        [string]$ProviderType,
        [long]$ResourceId,
        [string]$ProviderUsername,
        [string]$ProviderPassword
    )

    $providerUser = @($Users) | Where-Object { $_.username -eq $ProviderUsername } | Select-Object -First 1
    $providerBody = @{
        role = "PROVIDER"
        providerType = $ProviderType
        providerResourceId = $ResourceId
        enabled = $true
    }
    if ($null -eq $providerUser) {
        $providerBody.username = $ProviderUsername
        $providerBody.password = $ProviderPassword
        return Invoke-RestMethod -Uri "$BaseUrl/api/v1/users" -Method Post `
            -ContentType "application/json" -Headers $SupervisorHeaders -Body ($providerBody | ConvertTo-Json)
    }

    $providerUser = Invoke-RestMethod -Uri "$BaseUrl/api/v1/users/$($providerUser.id)" -Method Put `
        -ContentType "application/json" -Headers $SupervisorHeaders -Body ($providerBody | ConvertTo-Json)
    $resetBody = @{ newPassword = $ProviderPassword } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/users/$($providerUser.id)/password" -Method Patch `
        -ContentType "application/json" -Headers $SupervisorHeaders -Body $resetBody | Out-Null
    return $providerUser
}

function Invoke-ProviderSmoke {
    param(
        [string]$BaseUrl,
        [string]$ProviderType,
        [string]$ProviderUsername,
        [string]$ProviderPassword,
        [string]$OwnPath,
        [string]$DeniedPath
    )

    $loginBody = @{
        username = $ProviderUsername
        password = $ProviderPassword
    } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody
    if ($login.providerAccess.type -ne $ProviderType) {
        throw "El login de $ProviderUsername devolvio $($login.providerAccess.type) en lugar de $ProviderType"
    }

    $headers = @{ Authorization = "$($login.tokenType) $($login.accessToken)" }
    $ownResponse = Invoke-RestMethod -Uri "$BaseUrl$OwnPath" -Method Get -Headers $headers
    $deniedStatus = $null
    try {
        Invoke-RestMethod -Uri "$BaseUrl$DeniedPath" -Method Get -Headers $headers | Out-Null
        throw "El proveedor $ProviderType accedio indebidamente a $DeniedPath"
    }
    catch {
        $statusCode = Get-HttpStatusCode -ErrorRecord $_
        if ($statusCode -eq 403) {
            $deniedStatus = $statusCode
        }
        else {
            throw
        }
    }

    return [ordered]@{
        username = $login.username
        providerType = $login.providerAccess.type
        ownResultCount = @($ownResponse).Count
        incompatibleModuleStatus = $deniedStatus
    }
}

if (-not $SkipBuild) {
    .\mvnw -DskipTests package
}

if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "operador.demo"
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "123456"
}

if ([string]::IsNullOrWhiteSpace($SupervisorUsername)) {
    $SupervisorUsername = "supervisor.demo"
}

if ([string]::IsNullOrWhiteSpace($SupervisorPassword)) {
    $SupervisorPassword = "654321"
}

$jarPath = "target/costanorte-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    $legacyJarPath = "target/quadras-0.0.1-SNAPSHOT.jar"
    if (Test-Path $legacyJarPath) {
        $jarPath = $legacyJarPath
    } else {
        throw "Jar no encontrado en $jarPath ni en $legacyJarPath"
    }
}

$process = Start-Process -FilePath "java" `
    -ArgumentList "-jar", $jarPath, "--spring.profiles.active=$Profile", "--server.port=$Port", `
        "--costanorte.security.bootstrap-supervisor.enabled=true", `
        "--costanorte.security.bootstrap-supervisor.username=$SupervisorUsername", `
        "--costanorte.security.bootstrap-supervisor.password=$SupervisorPassword" `
    -PassThru `
    -WindowStyle Hidden `
    -RedirectStandardOutput "target/backend-smoke.out" `
    -RedirectStandardError "target/backend-smoke.err"

try {
    $health = $null
    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep -Seconds 2
        try {
            $health = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/system/health" -Method Get
            break
        } catch {
        }
    }

    if ($null -eq $health) {
        throw "Backend no respondio en /api/v1/system/health"
    }

    $loginBody = @{
        username = $Username
        password = $Password
    } | ConvertTo-Json

    $login = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
    $headers = @{
        Authorization = "$($login.tokenType) $($login.accessToken)"
    }

    $date = (Get-Date).ToString("yyyy-MM-dd")
    $createBody = @{
        guestName = "Smoke Script"
        reservationDate = $date
        startTime = "20:00:00"
        endTime = "21:00:00"
        notes = "Smoke local"
    } | ConvertTo-Json

    $created = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/reservations" -Method Post -ContentType "application/json" -Headers $headers -Body $createBody
    $reservationId = $created.id

    $updateBody = @{
        guestName = "Smoke Script Editado"
        reservationDate = $date
        startTime = "21:00:00"
        endTime = "22:00:00"
        notes = "Smoke local update"
    } | ConvertTo-Json

    $updated = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/reservations/$reservationId" -Method Put -ContentType "application/json" -Headers $headers -Body $updateBody
    $cancelled = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/reservations/$reservationId/cancel" -Method Patch -Headers $headers
    $list = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/reservations?reservationDate=$date" -Method Get -Headers $headers

    $supervisorLoginBody = @{
        username = $SupervisorUsername
        password = $SupervisorPassword
    } | ConvertTo-Json
    $supervisorLogin = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $supervisorLoginBody
    $supervisorHeaders = @{
        Authorization = "$($supervisorLogin.tokenType) $($supervisorLogin.accessToken)"
    }

    $baseUrl = "http://127.0.0.1:$Port"
    $users = Invoke-RestMethod -Uri "$baseUrl/api/v1/users" -Method Get -Headers $supervisorHeaders

    $courtCoaches = Invoke-RestMethod -Uri "$baseUrl/api/v1/courts/partner-coaches?activeOnly=true" `
        -Method Get -Headers $supervisorHeaders
    $massageProviders = Invoke-RestMethod -Uri "$baseUrl/api/v1/massages/providers?activeOnly=true" `
        -Method Get -Headers $supervisorHeaders
    $massageTherapists = @($massageProviders) | ForEach-Object {
        if ($_.active) {
            @($_.therapists) | Where-Object { $_.active }
        }
    }

    $maintenanceProviders = Invoke-RestMethod -Uri "$baseUrl/api/v1/maintenance/providers" -Method Get -Headers $supervisorHeaders
    $externalMaintenanceProviders = @($maintenanceProviders) | Where-Object {
        $_.providerType -eq "EXTERNAL" -and $_.active
    }
    $laundryProviders = Invoke-RestMethod -Uri "$baseUrl/api/v2/laundry/providers" -Method Get -Headers $supervisorHeaders
    $activeLaundryProviders = @($laundryProviders) | Where-Object { $_.active }

    $providerScenarios = @(
        [ordered]@{
            type = "COURTS"
            username = "smoke.provider.courts"
            resources = @($courtCoaches)
            ownPath = "/api/v1/provider/courts/bookings?dateFrom=$date&dateTo=$date"
            deniedPath = "/api/v1/provider/massages/bookings?dateFrom=$date&dateTo=$date"
        },
        [ordered]@{
            type = "MASSAGE"
            username = "smoke.provider.massage"
            resources = @($massageTherapists)
            ownPath = "/api/v1/provider/massages/bookings?dateFrom=$date&dateTo=$date"
            deniedPath = "/api/v1/provider/maintenance/orders?dateFrom=$date&dateTo=$date"
        },
        [ordered]@{
            type = "MAINTENANCE"
            username = $ProviderUsername
            resources = @($externalMaintenanceProviders)
            ownPath = "/api/v1/provider/maintenance/orders?dateFrom=$date&dateTo=$date"
            deniedPath = "/api/v1/provider/courts/bookings?dateFrom=$date&dateTo=$date"
        },
        [ordered]@{
            type = "LAUNDRY"
            username = "smoke.provider.laundry"
            resources = @($activeLaundryProviders)
            ownPath = "/api/v2/laundry/provider/documents?page=0&size=5"
            deniedPath = "/api/v1/provider/courts/bookings?dateFrom=$date&dateTo=$date"
        }
    )

    foreach ($scenario in $providerScenarios) {
        $resource = Select-ProviderResource -Users @($users) -ProviderType $scenario.type `
            -ProviderUsername $scenario.username -Resources @($scenario.resources)
        Set-SmokeProviderUser -BaseUrl $baseUrl -SupervisorHeaders $supervisorHeaders -Users @($users) `
            -ProviderType $scenario.type -ResourceId $resource.id -ProviderUsername $scenario.username `
            -ProviderPassword $ProviderPassword | Out-Null
        $users = Invoke-RestMethod -Uri "$baseUrl/api/v1/users" -Method Get -Headers $supervisorHeaders
    }

    $providerResults = foreach ($scenario in $providerScenarios) {
        Invoke-ProviderSmoke -BaseUrl $baseUrl -ProviderType $scenario.type `
            -ProviderUsername $scenario.username -ProviderPassword $ProviderPassword `
            -OwnPath $scenario.ownPath -DeniedPath $scenario.deniedPath
    }

    [ordered]@{
        date = $date
        healthStatus = $health.status
        authUser = $login.username
        authRole = $login.role
        reservationId = $reservationId
        createdStatus = $created.status
        updatedStatus = $updated.status
        cancelledStatus = $cancelled.status
        reservationsOnDate = @($list).Count
        providers = @($providerResults)
    } | ConvertTo-Json -Depth 5
}
finally {
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }
}
