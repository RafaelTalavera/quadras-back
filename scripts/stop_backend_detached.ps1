param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$JarRelativePath = "app\backend\costanorte.jar"
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

function Wait-UntilStopped {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock]$Query,
        [int]$TimeoutSeconds = 15
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $remaining = & $Query
        if (-not $remaining) {
            return
        }

        Start-Sleep -Milliseconds 500
    }
}

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$jarPath = Resolve-RequiredPath -Path (Join-Path $InstallRoot $JarRelativePath) -Label "Jar backend"

$processes = Get-CimInstance Win32_Process -Filter "name = 'java.exe'" |
    Where-Object { $_.CommandLine -like "*$jarPath*" }

$stoppedIds = @()
foreach ($process in $processes) {
    Stop-Process -Id $process.ProcessId -Force
    $stoppedIds += $process.ProcessId
}

Wait-UntilStopped -Query {
    Get-CimInstance Win32_Process -Filter "name = 'java.exe'" |
        Where-Object { $_.CommandLine -like "*$jarPath*" } |
        Select-Object -First 1
}

[ordered]@{
    jarPath = $jarPath
    stoppedCount = $stoppedIds.Count
    stoppedProcessIds = $stoppedIds
} | ConvertTo-Json -Depth 4
