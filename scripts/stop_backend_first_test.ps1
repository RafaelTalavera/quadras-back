param()

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot

$pidPath = "target/backend-first-test.pid"
if (-not (Test-Path $pidPath)) {
    Write-Output '{"stopped":false,"reason":"pid-file-not-found"}'
    exit 0
}

$rawPid = Get-Content $pidPath -ErrorAction SilentlyContinue | Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($rawPid)) {
    Remove-Item $pidPath -ErrorAction SilentlyContinue
    Write-Output '{"stopped":false,"reason":"pid-file-empty"}'
    exit 0
}

$process = Get-Process -Id ([int]$rawPid) -ErrorAction SilentlyContinue
if ($null -eq $process) {
    Remove-Item $pidPath -ErrorAction SilentlyContinue
    Write-Output '{"stopped":false,"reason":"process-not-running"}'
    exit 0
}

Stop-Process -Id $process.Id -Force
Remove-Item $pidPath -ErrorAction SilentlyContinue

[ordered]@{
    stopped = $true
    processId = $process.Id
} | ConvertTo-Json -Depth 2
