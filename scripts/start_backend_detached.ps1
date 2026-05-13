param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$JavaRelativePath = "runtime\jre\bin\java.exe",
    [string]$JarRelativePath = "app\backend\costanorte.jar",
    [string]$ConfigRelativeDir = "config",
    [string]$ProgramDataRoot = "$env:ProgramData\CostanorteLocal",
    [string]$JvmOptions = $env:COSTANORTE_JAVA_OPTS
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

function Convert-ToFileUri {
    param([string]$Path)

    return ([System.Uri]$Path).AbsoluteUri
}

function Get-ExistingBackendProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JarPath
    )

    $escapedJarPath = $JarPath.Replace('\', '\\')
    return Get-CimInstance Win32_Process -Filter "name = 'java.exe'" |
        Where-Object { $_.CommandLine -like "*$escapedJarPath*" } |
        Select-Object -First 1
}

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$jarPath = Resolve-RequiredPath -Path (Join-Path $InstallRoot $JarRelativePath) -Label "Jar backend"
$configDir = Resolve-RequiredPath -Path (Join-Path $InstallRoot $ConfigRelativeDir) -Label "Directorio de config"

$javaPath = Join-Path $InstallRoot $JavaRelativePath
if (-not (Test-Path $javaPath)) {
    $javaCommand = Get-Command "java.exe" -ErrorAction SilentlyContinue
    if ($null -eq $javaCommand) {
        throw "No se encontro java.exe ni en el bundle ni en el PATH."
    }

    $javaPath = $javaCommand.Source
}

$configUri = Convert-ToFileUri -Path ($configDir.TrimEnd("\") + "\")
$logsDir = Join-Path $ProgramDataRoot "logs\backend-detached"
New-Item -ItemType Directory -Path $logsDir -Force | Out-Null

$stdoutPath = Join-Path $logsDir "backend-stdout.log"
$stderrPath = Join-Path $logsDir "backend-stderr.log"

$existingProcess = Get-ExistingBackendProcess -JarPath $jarPath
if ($null -ne $existingProcess) {
    [ordered]@{
        startupMode = "already-running"
        processId = $existingProcess.ProcessId
        stdoutPath = $stdoutPath
        stderrPath = $stderrPath
        javaPath = $existingProcess.ExecutablePath
        jarPath = $jarPath
    } | ConvertTo-Json -Depth 4
    exit 0
}

$jvmArgs = if ([string]::IsNullOrWhiteSpace($JvmOptions)) {
    @("-Xms256m", "-Xmx512m", "-XX:NativeMemoryTracking=summary")
}
else {
    $JvmOptions -split "\s+" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
}

$argumentList = @(
    $jvmArgs
    "-jar"
    $jarPath
    "--spring.profiles.active=local"
    "--spring.config.additional-location=$configUri"
)

$process = Start-Process -FilePath $javaPath `
    -ArgumentList $argumentList `
    -PassThru `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutPath `
    -RedirectStandardError $stderrPath

[ordered]@{
    startupMode = "process"
    processId = $process.Id
    stdoutPath = $stdoutPath
    stderrPath = $stderrPath
    javaPath = $javaPath
    jarPath = $jarPath
} | ConvertTo-Json -Depth 4
