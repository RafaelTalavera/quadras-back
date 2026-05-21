param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [switch]$SkipHealthCheck
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
        [string]$Uri,
        [int]$TimeoutSeconds = 90
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

function Test-HealthEndpoint {
    param([string]$Uri)

    try {
        $response = Invoke-RestMethod -Uri $Uri -Method Get
        return $response.status -eq "UP"
    }
    catch {
        return $false
    }
}

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$scriptRoot = Join-Path $InstallRoot "scripts"
$provisionMySqlScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "provision_portable_mysql.ps1") -Label "provision_portable_mysql.ps1"
$installBackendServiceScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "install_backend_service.ps1") -Label "install_backend_service.ps1"
$startBackendDetachedScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "start_backend_detached.ps1") -Label "start_backend_detached.ps1"
$runtimeMySqlPath = Join-Path $InstallRoot "runtime\mysql"
$winSwPath = Join-Path $InstallRoot "tools\winsw\WinSW-x64.exe"

$summary = [ordered]@{
    installRoot = $InstallRoot
    mysql = $null
    backendService = $null
    health = $null
}

if (Test-Path $runtimeMySqlPath) {
    $summary.mysql = & $provisionMySqlScript -InstallRoot $InstallRoot | ConvertFrom-Json
}

if (Test-HealthEndpoint -Uri "http://127.0.0.1:8080/api/v1/system/health") {
    $summary.backendService = [pscustomobject]@{
        startupMode = "already-running"
        healthUri = "http://127.0.0.1:8080/api/v1/system/health"
    }
}
elseif (Test-Path $winSwPath) {
    $summary.backendService = & $installBackendServiceScript -InstallRoot $InstallRoot | ConvertFrom-Json
}
else {
    $summary.backendService = & $startBackendDetachedScript -InstallRoot $InstallRoot | ConvertFrom-Json
}

if (-not $SkipHealthCheck -and $null -ne $summary.backendService) {
    $summary.health = Wait-ForHealthEndpoint -Uri "http://127.0.0.1:8080/api/v1/system/health"
}

$summary | ConvertTo-Json -Depth 5
