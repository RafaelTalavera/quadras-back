param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$JavaRelativePath = "runtime\jre\bin\java.exe",
    [string]$JarRelativePath = "app\backend\costanorte.jar",
    [string]$ConfigRelativeDir = "config",
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

$jvmArgs = if ([string]::IsNullOrWhiteSpace($JvmOptions)) {
    @("-Xms256m", "-Xmx512m", "-XX:NativeMemoryTracking=summary")
}
else {
    $JvmOptions -split "\s+" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
}

& $javaPath @jvmArgs -jar $jarPath "--spring.profiles.active=local" "--spring.config.additional-location=$configUri"
