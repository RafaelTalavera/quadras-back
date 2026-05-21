param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent)
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

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$scriptRoot = Join-Path $InstallRoot "scripts"
$stopBackendScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "stop_backend_detached.ps1") -Label "stop_backend_detached.ps1"
$stopMySqlScript = Resolve-RequiredPath -Path (Join-Path $scriptRoot "stop_portable_mysql.ps1") -Label "stop_portable_mysql.ps1"
$runtimeMySqlPath = Join-Path $InstallRoot "runtime\mysql"

$summary = [ordered]@{
    installRoot = $InstallRoot
    backend = (& $stopBackendScript -InstallRoot $InstallRoot | ConvertFrom-Json)
    mysql = $null
}

if (Test-Path $runtimeMySqlPath) {
    $summary.mysql = & $stopMySqlScript -InstallRoot $InstallRoot | ConvertFrom-Json
}

$summary | ConvertTo-Json -Depth 5
