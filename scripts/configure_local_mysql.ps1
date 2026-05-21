param(
    [string]$MySqlBinDir,
    [string]$AdminUser = "root",
    [string]$AdminPassword = "",
    [string]$DatabaseName = "db_quadras",
    [string]$AppUser = "costanorte_app",
    [string]$AppPassword = "CostanorteDb2026!",
    [string]$DumpPath
)

$ErrorActionPreference = "Stop"

function Resolve-MySqlExe {
    param([string]$BinDir)

    if (-not [string]::IsNullOrWhiteSpace($BinDir)) {
        $candidate = Join-Path $BinDir "mysql.exe"
        if (Test-Path $candidate) {
            return (Resolve-Path $candidate).Path
        }
    }

    $command = Get-Command "mysql.exe" -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }

    throw "No se encontro mysql.exe. Pasar -MySqlBinDir o agregar MySQL al PATH."
}

function New-MySqlArgs {
    param(
        [string]$User,
        [string]$Password
    )

    $args = @("--protocol=TCP", "--user=$User")
    if (-not [string]::IsNullOrEmpty($Password)) {
        $args += "--password=$Password"
    }
    return $args
}

function Escape-SqlLiteral {
    param([string]$Value)

    return $Value.Replace("'", "''")
}

$mysqlExe = Resolve-MySqlExe -BinDir $MySqlBinDir
$authArgs = New-MySqlArgs -User $AdminUser -Password $AdminPassword

$escapedDatabaseName = Escape-SqlLiteral -Value $DatabaseName
$escapedAppUser = Escape-SqlLiteral -Value $AppUser
$escapedAppPassword = Escape-SqlLiteral -Value $AppPassword

$bootstrapSql = @"
CREATE DATABASE IF NOT EXISTS $DatabaseName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$escapedAppUser'@'localhost' IDENTIFIED BY '$escapedAppPassword';
CREATE USER IF NOT EXISTS '$escapedAppUser'@'127.0.0.1' IDENTIFIED BY '$escapedAppPassword';
ALTER USER '$escapedAppUser'@'localhost' IDENTIFIED BY '$escapedAppPassword';
ALTER USER '$escapedAppUser'@'127.0.0.1' IDENTIFIED BY '$escapedAppPassword';
GRANT ALL PRIVILEGES ON $DatabaseName.* TO '$escapedAppUser'@'localhost';
GRANT ALL PRIVILEGES ON $DatabaseName.* TO '$escapedAppUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@

$bootstrapSql | & $mysqlExe @authArgs

if (-not [string]::IsNullOrWhiteSpace($DumpPath)) {
    if (-not (Test-Path $DumpPath)) {
        throw "Dump no encontrado: $DumpPath"
    }

    Get-Content -Path $DumpPath | & $mysqlExe @authArgs $DatabaseName
}

[ordered]@{
    mysqlExe = $mysqlExe
    databaseName = $DatabaseName
    appUser = $AppUser
    importedDump = -not [string]::IsNullOrWhiteSpace($DumpPath)
} | ConvertTo-Json -Depth 4
