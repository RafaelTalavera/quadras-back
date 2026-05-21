param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$Username = $env:COSTANORTE_DEMO_USER_USERNAME,
    [string]$Password = $env:COSTANORTE_DEMO_USER_PASSWORD,
    [string]$HealthUri = "http://127.0.0.1:8080/api/v1/system/health",
    [string]$LoginUri = "http://127.0.0.1:8080/api/v1/auth/login",
    [int]$HealthTimeoutSeconds = 120,
    [switch]$KeepRunning
)

$ErrorActionPreference = "Stop"

function Resolve-RequiredPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Label
    )

    if (-not (Test-Path $Path)) {
        throw "$Label no encontrado: $Path"
    }

    return (Resolve-Path $Path).Path
}

function Wait-ForHealthEndpoint {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Uri,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-RestMethod -Uri $Uri -Method Get
            if ($response.status -eq "UP") {
                return $response
            }
        }
        catch {
        }

        Start-Sleep -Seconds 2
    }

    throw "Backend no respondio OK en $Uri"
}

if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "operador.demo"
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "Costanorte2026!"
}

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$scriptRoot = Resolve-RequiredPath -Path (Join-Path $InstallRoot "scripts") -Label "Directorio scripts"
$frontendExe = Resolve-RequiredPath -Path (Join-Path $InstallRoot "app\frontend\costanorte.exe") -Label "Frontend costanorte.exe"
$installScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "install_local_stack.ps1") -Label "install_local_stack.ps1"
$stopScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "stop_local_stack.ps1") -Label "stop_local_stack.ps1"

$summary = [ordered]@{
    installRoot = $InstallRoot
    frontendExecutable = $frontendExe
    stack = $null
    health = $null
    login = $null
    cleanup = $null
}

try {
    $summary.stack = & $installScript -InstallRoot $InstallRoot | ConvertFrom-Json
    $summary.health = Wait-ForHealthEndpoint -Uri $HealthUri -TimeoutSeconds $HealthTimeoutSeconds

    $loginBody = @{
        username = $Username
        password = $Password
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri $LoginUri -Method Post -ContentType "application/json" -Body $loginBody
    $summary.login = [ordered]@{
        username = $loginResponse.username
        role = $loginResponse.role
        tokenType = $loginResponse.tokenType
        accessTokenPresent = -not [string]::IsNullOrWhiteSpace($loginResponse.accessToken)
    }
}
finally {
    if (-not $KeepRunning) {
        try {
            $summary.cleanup = & $stopScript -InstallRoot $InstallRoot | ConvertFrom-Json
        }
        catch {
            $summary.cleanup = [ordered]@{
                error = $_.Exception.Message
            }
        }
    }
}

$summary | ConvertTo-Json -Depth 6
