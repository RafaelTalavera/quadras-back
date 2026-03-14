param(
    [int]$Port = 8091,
    [string]$Profile = "local",
    [switch]$SkipBuild,
    [string]$Username = $env:COSTANORTE_DEMO_USER_USERNAME,
    [string]$Password = $env:COSTANORTE_DEMO_USER_PASSWORD
)

$ErrorActionPreference = "Stop"

if (-not $SkipBuild) {
    .\mvnw -DskipTests package
}

if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "operador.demo"
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "Costanorte2026!"
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
    -ArgumentList "-jar", $jarPath, "--spring.profiles.active=$Profile", "--server.port=$Port" `
    -PassThru `
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
    } | ConvertTo-Json -Depth 5
}
finally {
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }
}
