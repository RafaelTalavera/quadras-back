param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$ServiceName = "CostanorteBackend",
    [string]$DisplayName = "COSTANORTE Backend Local",
    [string]$Description = "Backend local de COSTANORTE para operacion Windows.",
    [string]$JavaRelativePath = "runtime\jre\bin\java.exe",
    [string]$JarRelativePath = "app\backend\costanorte.jar",
    [string]$ConfigRelativeDir = "config",
    [string]$WinSwRelativePath = "tools\winsw\WinSW-x64.exe",
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

function Convert-ToFileUri {
    param([string]$Path)

    return ([System.Uri]$Path).AbsoluteUri
}

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$jarPath = Resolve-RequiredPath -Path (Join-Path $InstallRoot $JarRelativePath) -Label "Jar backend"
$configDir = Resolve-RequiredPath -Path (Join-Path $InstallRoot $ConfigRelativeDir) -Label "Directorio de config"
$winSwSource = Resolve-RequiredPath -Path (Join-Path $InstallRoot $WinSwRelativePath) -Label "WinSW"

$javaPath = Join-Path $InstallRoot $JavaRelativePath
if (-not (Test-Path $javaPath)) {
    $javaCommand = Get-Command "java.exe" -ErrorAction SilentlyContinue
    if ($null -eq $javaCommand) {
        throw "No se encontro java.exe ni en el bundle ni en el PATH."
    }

    $javaPath = $javaCommand.Source
}

$serviceRoot = Join-Path $ProgramDataRoot "service"
$logsDir = Join-Path $ProgramDataRoot "logs\backend-service"
New-Item -ItemType Directory -Path $serviceRoot -Force | Out-Null
New-Item -ItemType Directory -Path $logsDir -Force | Out-Null

$serviceExe = Join-Path $serviceRoot "$ServiceName.exe"
$serviceXml = Join-Path $serviceRoot "$ServiceName.xml"

Copy-Item -Path $winSwSource -Destination $serviceExe -Force

$configUri = Convert-ToFileUri -Path ($configDir.TrimEnd("\") + "\")

$xml = @"
<service>
  <id>$ServiceName</id>
  <name>$DisplayName</name>
  <description>$Description</description>
  <workingdirectory>$InstallRoot</workingdirectory>
  <executable>$javaPath</executable>
  <arguments>-jar "$jarPath" --spring.profiles.active=local --spring.config.additional-location=$configUri</arguments>
  <logpath>$logsDir</logpath>
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold>
    <keepFiles>8</keepFiles>
  </log>
  <onfailure action="restart" delay="10 sec" />
  <resetfailure>1 hour</resetfailure>
</service>
"@

$xml | Set-Content -Path $serviceXml

& $serviceExe stop | Out-Null
& $serviceExe uninstall | Out-Null
& $serviceExe install | Out-Null
& $serviceExe start | Out-Null

[ordered]@{
    serviceName = $ServiceName
    serviceExe = $serviceExe
    javaPath = $javaPath
    jarPath = $jarPath
    configDir = $configDir
    serviceRoot = $serviceRoot
} | ConvertTo-Json -Depth 4
