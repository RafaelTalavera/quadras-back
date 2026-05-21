param(
    [int]$Port = 8091,
    [string]$Profile = "smoke",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot

if (-not $SkipBuild) {
    .\mvnw -DskipTests package | Out-Host
}

$jarPath = "target/costanorte-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    $legacyJarPath = "target/quadras-0.0.1-SNAPSHOT.jar"
    if (Test-Path $legacyJarPath) {
        $jarPath = $legacyJarPath
    }
    else {
        throw "Jar no encontrado en $jarPath ni en $legacyJarPath"
    }
}

$stdoutPath = "target/backend-first-test.out"
$stderrPath = "target/backend-first-test.err"
$pidPath = "target/backend-first-test.pid"

$existingPid = $null
if (Test-Path $pidPath) {
    $rawPid = (Get-Content $pidPath -ErrorAction SilentlyContinue | Select-Object -First 1)
    if (-not [string]::IsNullOrWhiteSpace($rawPid)) {
        $existingPid = [int]$rawPid
        $existingProcess = Get-Process -Id $existingPid -ErrorAction SilentlyContinue
        if ($null -ne $existingProcess) {
            [ordered]@{
                startupMode = "already-running"
                processId = $existingPid
                profile = $Profile
                baseUrl = "http://127.0.0.1:$Port/api/v1"
                username = ${env:COSTANORTE_DEMO_USER_USERNAME}
                password = if ([string]::IsNullOrWhiteSpace($env:COSTANORTE_DEMO_USER_PASSWORD)) { "Costanorte2026!" } else { $env:COSTANORTE_DEMO_USER_PASSWORD }
                stdoutPath = $stdoutPath
                stderrPath = $stderrPath
            } | ConvertTo-Json -Depth 4
            exit 0
        }
    }
}

$process = Start-Process -FilePath "java" `
    -ArgumentList "-jar", $jarPath, "--spring.profiles.active=$Profile", "--server.port=$Port" `
    -PassThru `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutPath `
    -RedirectStandardError $stderrPath

Set-Content -Path $pidPath -Value $process.Id

$health = $null
for ($i = 0; $i -lt 45; $i++) {
    Start-Sleep -Seconds 2
    try {
        $health = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/v1/system/health" -Method Get -TimeoutSec 5
        break
    }
    catch {
    }
}

if ($null -eq $health) {
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }
    Remove-Item $pidPath -ErrorAction SilentlyContinue
    throw "Backend no respondio en /api/v1/system/health. Revisar $stdoutPath y $stderrPath"
}

$username = if ([string]::IsNullOrWhiteSpace($env:COSTANORTE_DEMO_USER_USERNAME)) {
    "operador.demo"
}
else {
    $env:COSTANORTE_DEMO_USER_USERNAME
}

$password = if ([string]::IsNullOrWhiteSpace($env:COSTANORTE_DEMO_USER_PASSWORD)) {
    "Costanorte2026!"
}
else {
    $env:COSTANORTE_DEMO_USER_PASSWORD
}

[ordered]@{
    startupMode = "started"
    processId = $process.Id
    profile = $Profile
    baseUrl = "http://127.0.0.1:$Port/api/v1"
    healthStatus = $health.status
    username = $username
    password = $password
    stdoutPath = $stdoutPath
    stderrPath = $stderrPath
} | ConvertTo-Json -Depth 4
