param(
    [string]$BundleDir,
    [string]$IsccPath
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path $PSScriptRoot -Parent

if ([string]::IsNullOrWhiteSpace($BundleDir)) {
    $BundleDir = Join-Path $repoRoot "dist\windows-local"
}

if (-not (Test-Path $BundleDir)) {
    throw "Bundle no encontrado: $BundleDir"
}

$bundleDir = (Resolve-Path $BundleDir).Path
$issPath = Join-Path $bundleDir "costanorte-local.iss"
if (-not (Test-Path $issPath)) {
    throw "No se encontro el archivo ISS en $issPath"
}

if ([string]::IsNullOrWhiteSpace($IsccPath)) {
    $command = Get-Command "iscc.exe" -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        $IsccPath = $command.Source
    }
}

if ([string]::IsNullOrWhiteSpace($IsccPath)) {
    throw "No se encontro iscc.exe. Instalar Inno Setup o pasar -IsccPath."
}

$resolvedIscc = (Resolve-Path $IsccPath).Path

Push-Location $bundleDir
try {
    & $resolvedIscc $issPath
}
finally {
    Pop-Location
}

$outputDir = Join-Path $bundleDir "output"
$installer = Get-ChildItem -Path $outputDir -Filter "*.exe" -File -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

[ordered]@{
    bundleDir = $bundleDir
    isccPath = $resolvedIscc
    outputDir = $outputDir
    installer = if ($null -ne $installer) { $installer.FullName } else { $null }
} | ConvertTo-Json -Depth 4
