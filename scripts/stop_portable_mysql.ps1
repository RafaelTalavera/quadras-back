param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$ServiceName = "CostanorteMySQL",
    [string]$ConfigRelativePath = "config\mysql-service.ini",
    [string]$ProgramDataRoot = "$env:ProgramData\CostanorteLocal"
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
        [int]$TimeoutSeconds = 20
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
$programDataConfigPath = Join-Path $ProgramDataRoot "config\mysql-service.ini"
$legacyConfigPath = Join-Path $InstallRoot $ConfigRelativePath

if (Test-Path $programDataConfigPath) {
    $configPath = (Resolve-Path $programDataConfigPath).Path
}
else {
    $configPath = Resolve-RequiredPath -Path $legacyConfigPath -Label "Config MySQL"
}

$service = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
$serviceStopped = $false
if ($null -ne $service -and $service.Status -eq "Running") {
    Stop-Service -Name $ServiceName -Force
    $serviceStopped = $true
}

$processes = Get-CimInstance Win32_Process -Filter "name = 'mysqld.exe'" |
    Where-Object { $_.CommandLine -like "*$configPath*" }

$stoppedIds = @()
foreach ($process in $processes) {
    Stop-Process -Id $process.ProcessId -Force
    $stoppedIds += $process.ProcessId
}

Wait-UntilStopped -Query {
    Get-CimInstance Win32_Process -Filter "name = 'mysqld.exe'" |
        Where-Object { $_.CommandLine -like "*$configPath*" } |
        Select-Object -First 1
}

[ordered]@{
    configPath = $configPath
    serviceName = $ServiceName
    serviceStopped = $serviceStopped
    stoppedCount = $stoppedIds.Count
    stoppedProcessIds = $stoppedIds
} | ConvertTo-Json -Depth 4
