param(
    [string]$FrontendRepoPath,
    [string]$OutputDir,
    [string]$ApiBaseUrl = "http://127.0.0.1:8080/api/v1",
    [string]$JavaHome,
    [string]$MySqlServerDir,
    [string]$WinSwPath,
    [string]$DatabaseDumpPath,
    [int]$MySqlPort = 3307,
    [switch]$SkipBackendBuild,
    [switch]$SkipFrontendBuild
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path $PSScriptRoot -Parent

if ([string]::IsNullOrWhiteSpace($FrontendRepoPath)) {
    $FrontendRepoPath = Join-Path (Split-Path $repoRoot -Parent) "quedras-front"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $repoRoot "dist\windows-local"
}

function Resolve-JavaHomeForBundle {
    param()

    if (-not [string]::IsNullOrWhiteSpace($JavaHome)) {
        return Resolve-RequiredPath -Path $JavaHome -Label "JavaHome"
    }

    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME) -and (Test-Path $env:JAVA_HOME)) {
        return (Resolve-Path $env:JAVA_HOME).Path
    }

    $javaCommand = Get-Command "java.exe" -ErrorAction SilentlyContinue
    if ($null -ne $javaCommand) {
        $javaBinDir = Split-Path $javaCommand.Source -Parent
        $candidateJavaHome = Split-Path $javaBinDir -Parent
        if (Test-Path $candidateJavaHome) {
            return (Resolve-Path $candidateJavaHome).Path
        }
    }

    throw "No se encontro un Java 17 local para incluir en el bundle. Configurar JAVA_HOME o pasar -JavaHome."
}

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

function Copy-DirectoryContent {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Source,
        [Parameter(Mandatory = $true)]
        [string]$Destination
    )

    if (-not (Test-Path $Destination)) {
        New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    }

    Copy-Item -Path (Join-Path $Source "*") -Destination $Destination -Recurse -Force
}

function Copy-OptionalFile {
    param(
        [string]$Source,
        [string]$Destination
    )

    if ([string]::IsNullOrWhiteSpace($Source)) {
        return $false
    }

    if (-not (Test-Path $Source)) {
        throw "Archivo opcional no encontrado: $Source"
    }

    Copy-Item -Path $Source -Destination $Destination -Force
    return $true
}

function Set-BackendDatasourcePort {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ConfigPath,
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    $content = Get-Content -Path $ConfigPath -Raw
    $updated = [System.Text.RegularExpressions.Regex]::Replace(
        $content,
        'jdbc:mysql://127\.0\.0\.1:\d+/',
        "jdbc:mysql://127.0.0.1:$Port/"
    )

    Set-Content -Path $ConfigPath -Value $updated
}

$FrontendRepoPath = Resolve-RequiredPath -Path $FrontendRepoPath -Label "Repositorio frontend"

if (-not $SkipBackendBuild) {
    Push-Location $repoRoot
    try {
        .\mvnw -DskipTests package
    }
    finally {
        Pop-Location
    }
}

if (-not $SkipFrontendBuild) {
    Push-Location $FrontendRepoPath
    try {
        flutter build windows --release --dart-define="COSTANORTE_API_BASE_URL=$ApiBaseUrl"
    }
    finally {
        Pop-Location
    }
}

$backendJar = Join-Path $repoRoot "target\costanorte-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $backendJar)) {
    $candidateJar = Get-ChildItem -Path (Join-Path $repoRoot "target") -Filter "*.jar" -File |
        Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*original*" } |
        Select-Object -First 1

    if ($null -eq $candidateJar) {
        throw "No se encontro un jar de backend en target"
    }

    $backendJar = $candidateJar.FullName
}

$frontendReleaseDir = Join-Path $FrontendRepoPath "build\windows\x64\runner\Release"
if (-not (Test-Path $frontendReleaseDir)) {
    throw "No se encontro el release de Windows del frontend en $frontendReleaseDir"
}

$templateRoot = Join-Path $repoRoot "installer\windows-local"
$templateRoot = Resolve-RequiredPath -Path $templateRoot -Label "Template de instalador"

if (Test-Path $OutputDir) {
    Remove-Item -Path $OutputDir -Recurse -Force
}

$stagingDirs = @(
    "app\backend",
    "app\frontend",
    "config",
    "database\seed",
    "installer",
    "runtime",
    "scripts",
    "support\scripts",
    "tools\winsw"
)

foreach ($relativeDir in $stagingDirs) {
    New-Item -ItemType Directory -Path (Join-Path $OutputDir $relativeDir) -Force | Out-Null
}

Copy-Item -Path $backendJar -Destination (Join-Path $OutputDir "app\backend\costanorte.jar") -Force
Copy-DirectoryContent -Source $frontendReleaseDir -Destination (Join-Path $OutputDir "app\frontend")
Copy-DirectoryContent -Source (Join-Path $templateRoot "config") -Destination (Join-Path $OutputDir "config")
Copy-DirectoryContent -Source (Join-Path $templateRoot "installer") -Destination (Join-Path $OutputDir "installer")
Copy-Item -Path (Join-Path $templateRoot "README.md") -Destination (Join-Path $OutputDir "README.md") -Force
Copy-Item -Path (Join-Path $templateRoot "costanorte-local.iss") -Destination (Join-Path $OutputDir "costanorte-local.iss") -Force

$bundleScripts = @(
    "build_windows_installer.ps1",
    "configure_local_mysql.ps1",
    "install_local_stack.ps1",
    "install_backend_service.ps1",
    "provision_portable_mysql.ps1",
    "smoke_windows_local_bundle.ps1",
    "stop_backend_detached.ps1",
    "stop_local_stack.ps1",
    "stop_portable_mysql.ps1",
    "start_backend_detached.ps1",
    "start_backend_console.ps1"
)

foreach ($scriptName in $bundleScripts) {
    $scriptSource = Join-Path $PSScriptRoot $scriptName
    Copy-Item -Path $scriptSource -Destination (Join-Path $OutputDir "scripts\$scriptName") -Force
    Copy-Item -Path $scriptSource -Destination (Join-Path $OutputDir "support\scripts\$scriptName") -Force
}

Copy-Item -Path (Join-Path $templateRoot "README.md") -Destination (Join-Path $OutputDir "support\README.md") -Force
Copy-Item -Path (Join-Path $templateRoot "GUIA_COLEGA_WINDOWS.md") -Destination (Join-Path $OutputDir "support\GUIA_COLEGA_WINDOWS.md") -Force

$mysqlBundled = $false
if (-not [string]::IsNullOrWhiteSpace($MySqlServerDir)) {
    $resolvedMySqlServerDir = Resolve-RequiredPath -Path $MySqlServerDir -Label "MySqlServerDir"
    Copy-DirectoryContent -Source $resolvedMySqlServerDir -Destination (Join-Path $OutputDir "runtime\mysql")
    $mysqlBundled = $true
}

$resolvedJavaHome = Resolve-JavaHomeForBundle
Copy-DirectoryContent -Source $resolvedJavaHome -Destination (Join-Path $OutputDir "runtime\jre")
$javaBundled = $true

$winSwBundled = Copy-OptionalFile -Source $WinSwPath -Destination (Join-Path $OutputDir "tools\winsw\WinSW-x64.exe")
$dumpBundled = $false
if (-not [string]::IsNullOrWhiteSpace($DatabaseDumpPath)) {
    $dumpBundled = Copy-OptionalFile -Source $DatabaseDumpPath -Destination (Join-Path $OutputDir "database\seed\baseline.sql")
}

$backendConfigPath = Join-Path $OutputDir "config\application-local.properties"
Set-BackendDatasourcePort -ConfigPath $backendConfigPath -Port $MySqlPort

$manifest = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:sszzz")
    backendJar = "app/backend/costanorte.jar"
    frontendExecutable = "app/frontend/costanorte.exe"
    apiBaseUrl = $ApiBaseUrl
    mysqlPort = $MySqlPort
    frontendRepoPath = $FrontendRepoPath
    includes = [ordered]@{
        javaRuntime = $javaBundled
        mysqlRuntime = $mysqlBundled
        winSw = $winSwBundled
        databaseDump = $dumpBundled
    }
    nextSteps = @(
        "Revisar config/application-local.properties antes de distribuir.",
        "Si se quiere MySQL portable dentro del instalador, repetir el bundle con -MySqlServerDir apuntando a un MySQL noinstall ZIP ya extraido.",
        "Si se quiere backend como servicio Windows, agregar WinSW-x64.exe o repetir el bundle con -WinSwPath.",
        "Si se quiere una BD inicial real, repetir el bundle con -DatabaseDumpPath."
    )
}

$manifest | ConvertTo-Json -Depth 5 | Set-Content -Path (Join-Path $OutputDir "bundle-manifest.json")

Write-Host "Bundle generado en: $OutputDir"
Write-Host "Frontend: $(Join-Path $OutputDir 'app\frontend\costanorte.exe')"
Write-Host "Backend: $(Join-Path $OutputDir 'app\backend\costanorte.jar')"
