#define MyAppName "COSTANORTE Local"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Axioma"
#define MyAppExeName "costanorte.exe"

[Setup]
AppId={{D6B8A0B0-0C36-469B-93A8-BBBA88B9341F}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\Costanorte Local
DisableProgramGroupPage=yes
PrivilegesRequired=admin
OutputDir=output
OutputBaseFilename=costanorte-local-installer
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Files]
Source: "app\frontend\*"; DestDir: "{app}\app\frontend"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "app\backend\*"; DestDir: "{app}\app\backend"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "config\*"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "database\seed\*"; DestDir: "{app}\database\seed"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist
Source: "runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist
Source: "scripts\*"; DestDir: "{app}\scripts"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "support\*"; DestDir: "{app}\support"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "tools\winsw\*"; DestDir: "{app}\tools\winsw"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist

[Icons]
Name: "{autoprograms}\COSTANORTE\Abrir COSTANORTE"; Filename: "{app}\app\frontend\{#MyAppExeName}"
Name: "{autoprograms}\COSTANORTE\Instalar stack local"; Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\support\scripts\install_local_stack.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\COSTANORTE\Detener stack local"; Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\support\scripts\stop_local_stack.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\COSTANORTE\Iniciar backend local"; Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\support\scripts\start_backend_console.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\COSTANORTE\Configurar base local"; Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\support\scripts\configure_local_mysql.ps1"""
Name: "{autodesktop}\COSTANORTE"; Filename: "{app}\app\frontend\{#MyAppExeName}"

[Run]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\support\scripts\install_local_stack.ps1"" -InstallRoot ""{app}"""; Description: "Provisionar base y backend local"; Flags: waituntilterminated postinstall skipifsilent
Filename: "{app}\app\frontend\{#MyAppExeName}"; Description: "Abrir COSTANORTE"; Flags: nowait postinstall skipifsilent unchecked

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\support\scripts\stop_local_stack.ps1"" -InstallRoot ""{app}"""; RunOnceId: "StopLocalStack"; Flags: waituntilterminated skipifdoesntexist

[UninstallDelete]
Type: files; Name: "{commonappdata}\CostanorteLocal\config\mysql-initialize.ini"
Type: files; Name: "{commonappdata}\CostanorteLocal\config\mysql-service.ini"
Type: filesandordirs; Name: "{commonappdata}\CostanorteLocal\logs"
Type: filesandordirs; Name: "{commonappdata}\CostanorteLocal\mysql"
Type: filesandordirs; Name: "{commonappdata}\CostanorteLocal\service"

[Code]
procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    MsgBox(
      'Instalacion base completada.' + #13#10 + #13#10 +
      'Siguiente paso recomendado:' + #13#10 +
      '1. Ejecutar "Instalar stack local" si el bundle incluye MySQL portable y WinSW.' + #13#10 +
      '2. Si no, ejecutar "Configurar base local" y luego "Iniciar backend local".' + #13#10 +
      '3. Abrir COSTANORTE.',
      mbInformation,
      MB_OK
    );
  end;
end;
