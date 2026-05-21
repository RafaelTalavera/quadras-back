param(
    [string]$InstallRoot = (Split-Path $PSScriptRoot -Parent),
    [string]$MySqlRelativeDir = "runtime\mysql",
    [string]$ServiceName = "CostanorteMySQL",
    [int]$Port = 3307,
    [string]$DatabaseName = "db_quadras",
    [string]$RootPassword = "CostanorteRoot2026!",
    [string]$AppUser = "costanorte_app",
    [string]$AppPassword = "CostanorteDb2026!",
    [string]$DumpRelativePath = "database\seed\baseline.sql",
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

function Convert-ToMySqlPath {
    param([string]$Path)

    return $Path.Replace("\", "/")
}

function Escape-SqlLiteral {
    param([string]$Value)

    return $Value.Replace("'", "''")
}

function Format-CommandArgument {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Argument
    )

    if ($Argument -match '[\s"]') {
        return '"' + ($Argument -replace '"', '\"') + '"'
    }

    return $Argument
}

function Invoke-NativeProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $stdoutFile = [System.IO.Path]::GetTempFileName()
    $stderrFile = [System.IO.Path]::GetTempFileName()

    try {
        $argumentLine = ($Arguments | ForEach-Object { Format-CommandArgument -Argument $_ }) -join " "
        $process = Start-Process -FilePath $Executable `
            -ArgumentList $argumentLine `
            -Wait `
            -PassThru `
            -NoNewWindow `
            -RedirectStandardOutput $stdoutFile `
            -RedirectStandardError $stderrFile

        $stdout = if (Test-Path $stdoutFile) { Get-Content -Path $stdoutFile -Raw } else { "" }
        $stderr = if (Test-Path $stderrFile) { Get-Content -Path $stderrFile -Raw } else { "" }
        $output = ($stdout + [Environment]::NewLine + $stderr).Trim()

        if ($process.ExitCode -ne 0) {
            throw "Comando fallo (`"$Executable`"): $output"
        }

        return $output
    }
    finally {
        Remove-Item -Path $stdoutFile, $stderrFile -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-MySqlImport {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,
        [Parameter(Mandatory = $true)]
        [string]$InputFile
    )

    $commandLine = @(
        Format-CommandArgument -Argument $Executable
        ($Arguments | ForEach-Object { Format-CommandArgument -Argument $_ })
    ) -join " "

    $cmdExpression = "$commandLine < $(Format-CommandArgument -Argument $InputFile)"
    return Invoke-NativeProcess -Executable "cmd.exe" -Arguments @("/d", "/c", $cmdExpression)
}

function Invoke-MySqlCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $output = Invoke-NativeProcess -Executable $Executable -Arguments $Arguments
    if ($null -eq $output) {
        return ""
    }

    return $output
}

function Test-MySqlConnection {
    param(
        [string]$MysqlExe,
        [int]$Port,
        [string]$Password
    )

    $arguments = @(
        "--protocol=tcp",
        "--host=localhost",
        "--port=$Port",
        "--user=root",
        "--batch",
        "--skip-column-names",
        "-e",
        "SELECT 1;"
    )

    if (-not [string]::IsNullOrEmpty($Password)) {
        $arguments += "--password=$Password"
    }

    try {
        $result = Invoke-MySqlCommand -Executable $MysqlExe -Arguments $arguments
        return $result -match '(^|[\r\n])1([\r\n]|$)'
    }
    catch {
        return $false
    }
}

function Wait-ForTcpPort {
    param(
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutSeconds = 60
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $client = New-Object System.Net.Sockets.TcpClient
        try {
            $async = $client.BeginConnect($HostName, $Port, $null, $null)
            if ($async.AsyncWaitHandle.WaitOne(1000) -and $client.Connected) {
                $client.EndConnect($async)
                return
            }
        }
        catch {
        }
        finally {
            $client.Dispose()
        }

        Start-Sleep -Seconds 1
    }

    throw "MySQL no respondio en ${HostName}:$Port dentro del tiempo esperado."
}

function Test-TcpPortListening {
    param(
        [string]$HostName,
        [int]$Port
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($HostName, $Port, $null, $null)
        if ($async.AsyncWaitHandle.WaitOne(1000) -and $client.Connected) {
            $client.EndConnect($async)
            return $true
        }
    }
    catch {
    }
    finally {
        $client.Dispose()
    }

    return $false
}

function Start-DetachedMySqlProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string]$DefaultsFile,
        [Parameter(Mandatory = $true)]
        [string]$LogDir
    )

    New-Item -ItemType Directory -Path $LogDir -Force | Out-Null

    $stdoutPath = Join-Path $LogDir "mysql-stdout.log"
    $stderrPath = Join-Path $LogDir "mysql-stderr.log"
    $argumentLine = @(
        Format-CommandArgument -Argument "--defaults-file=$DefaultsFile"
        "--console"
    ) -join " "

    $process = Start-Process -FilePath $Executable `
        -ArgumentList $argumentLine `
        -PassThru `
        -WindowStyle Hidden `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath

    return $process
}

$InstallRoot = Resolve-RequiredPath -Path $InstallRoot -Label "InstallRoot"
$mysqlRoot = Resolve-RequiredPath -Path (Join-Path $InstallRoot $MySqlRelativeDir) -Label "MySQL runtime"
$mysqldExe = Resolve-RequiredPath -Path (Join-Path $mysqlRoot "bin\mysqld.exe") -Label "mysqld.exe"
$mysqlExe = Resolve-RequiredPath -Path (Join-Path $mysqlRoot "bin\mysql.exe") -Label "mysql.exe"

$programDataMySqlRoot = Join-Path $ProgramDataRoot "mysql"
$programDataConfigRoot = Join-Path $ProgramDataRoot "config"
$dataDir = Join-Path $programDataMySqlRoot "data"
$logDir = Join-Path $programDataMySqlRoot "logs"
$tmpDir = Join-Path $programDataMySqlRoot "tmp"
$configDir = $programDataConfigRoot
$serviceConfigPath = Join-Path $configDir "mysql-service.ini"
$initializeConfigPath = Join-Path $configDir "mysql-initialize.ini"
$dumpPath = Join-Path $InstallRoot $DumpRelativePath
$hasDump = Test-Path $dumpPath
$dataInitialized = Test-Path (Join-Path $dataDir "mysql")

New-Item -ItemType Directory -Path $ProgramDataRoot -Force | Out-Null
New-Item -ItemType Directory -Path $programDataMySqlRoot -Force | Out-Null
New-Item -ItemType Directory -Path $programDataConfigRoot -Force | Out-Null
New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null

$mysqlRootConfigPath = Convert-ToMySqlPath -Path $mysqlRoot
$dataDirConfigPath = Convert-ToMySqlPath -Path $dataDir
$logDirConfigPath = Convert-ToMySqlPath -Path $logDir
$tmpDirConfigPath = Convert-ToMySqlPath -Path $tmpDir

@"
[mysqld]
basedir=$mysqlRootConfigPath
datadir=$dataDirConfigPath
"@ | Set-Content -Path $initializeConfigPath

@"
[mysqld]
basedir=$mysqlRootConfigPath
datadir=$dataDirConfigPath
port=$Port
log-error=$logDirConfigPath/mysql.err
pid-file=$logDirConfigPath/mysql.pid
tmpdir=$tmpDirConfigPath
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
explicit_defaults_for_timestamp=ON
"@ | Set-Content -Path $serviceConfigPath

if (-not $dataInitialized) {
    Invoke-MySqlCommand -Executable $mysqldExe -Arguments @(
        "--defaults-file=$initializeConfigPath",
        "--initialize-insecure",
        "--console"
    ) | Out-Null
}

$startupMode = "process"
$serviceInstallAttempted = $false
$detachedProcess = $null
if (Test-TcpPortListening -HostName "127.0.0.1" -Port $Port) {
    $startupMode = "already-running"
}

if ($startupMode -ne "already-running") {
    $existingService = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
    if ($null -eq $existingService) {
        try {
            $serviceInstallAttempted = $true
            Invoke-MySqlCommand -Executable $mysqldExe -Arguments @(
                "--install",
                $ServiceName,
                "--defaults-file=$serviceConfigPath"
            ) | Out-Null
            $existingService = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
        }
        catch {
            $existingService = $null
        }
    }

    if ($null -ne $existingService) {
        try {
            if ($existingService.Status -ne "Running") {
                Start-Service -Name $ServiceName
            }

            $startupMode = "service"
        }
        catch {
            $existingService = $null
        }
    }

    if ($null -eq $existingService) {
        $detachedProcess = Start-DetachedMySqlProcess -Executable $mysqldExe -DefaultsFile $serviceConfigPath -LogDir $logDir
    }
}

Wait-ForTcpPort -HostName "127.0.0.1" -Port $Port

$rootPasswordReady = Test-MySqlConnection -MysqlExe $mysqlExe -Port $Port -Password $RootPassword
$initialRootPassword = if ($rootPasswordReady) { $RootPassword } else { "" }

$escapedRootPassword = Escape-SqlLiteral -Value $RootPassword
$escapedDatabaseName = Escape-SqlLiteral -Value $DatabaseName
$escapedAppUser = Escape-SqlLiteral -Value $AppUser
$escapedAppPassword = Escape-SqlLiteral -Value $AppPassword

$bootstrapSql = @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$escapedRootPassword';
CREATE DATABASE IF NOT EXISTS $DatabaseName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$escapedAppUser'@'localhost' IDENTIFIED BY '$escapedAppPassword';
CREATE USER IF NOT EXISTS '$escapedAppUser'@'127.0.0.1' IDENTIFIED BY '$escapedAppPassword';
ALTER USER '$escapedAppUser'@'localhost' IDENTIFIED BY '$escapedAppPassword';
ALTER USER '$escapedAppUser'@'127.0.0.1' IDENTIFIED BY '$escapedAppPassword';
GRANT ALL PRIVILEGES ON $DatabaseName.* TO '$escapedAppUser'@'localhost';
GRANT ALL PRIVILEGES ON $DatabaseName.* TO '$escapedAppUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@

$bootstrapArgs = @(
    "--protocol=tcp",
    "--host=localhost",
    "--port=$Port",
    "--user=root",
    "-e",
    ($bootstrapSql -replace "(\r?\n)+", " ")
)

if (-not [string]::IsNullOrEmpty($initialRootPassword)) {
    $bootstrapArgs += "--password=$initialRootPassword"
}

Invoke-MySqlCommand -Executable $mysqlExe -Arguments $bootstrapArgs | Out-Null

$tableCount = Invoke-MySqlCommand -Executable $mysqlExe -Arguments @(
    "--protocol=tcp",
    "--host=localhost",
    "--port=$Port",
    "--user=root",
    "--password=$RootPassword",
    "--batch",
    "--skip-column-names",
    "-e",
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$escapedDatabaseName';"
)

$importedDump = $false
if ($hasDump -and [int]$tableCount -eq 0) {
    Invoke-MySqlImport -Executable $mysqlExe -Arguments @(
        "--protocol=tcp",
        "--host=localhost",
        "--port=$Port",
        "--user=root",
        "--password=$RootPassword",
        $DatabaseName
    ) -InputFile $dumpPath | Out-Null
    $importedDump = $true
}

[ordered]@{
    serviceName = $ServiceName
    startupMode = $startupMode
    serviceInstallAttempted = $serviceInstallAttempted
    mysqlRoot = $mysqlRoot
    dataDir = $dataDir
    port = $Port
    databaseName = $DatabaseName
    appUser = $AppUser
    importedDump = $importedDump
    serviceConfigPath = $serviceConfigPath
    detachedProcessId = if ($null -ne $detachedProcess) { $detachedProcess.Id } else { $null }
} | ConvertTo-Json -Depth 4
