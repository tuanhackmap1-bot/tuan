param(
  [string]$SourceApk = (Join-Path $PSScriptRoot "TTS_TOOL_V12.1_BYPASS_NO_KEY.apk")
)
$ErrorActionPreference = "Stop"
$Work = $PSScriptRoot

function MustRun([string]$File,[string[]]$Arguments) {
  & $File @Arguments
  if ($LASTEXITCODE -ne 0) { throw "$File failed with exit code $LASTEXITCODE" }
}
function GetVerified([string]$Url,[string]$Out,[string]$Hash,[string]$Algorithm) {
  if (!(Test-Path $Out)) {
    Write-Host "Downloading $Url"
    Invoke-WebRequest -Uri $Url -OutFile $Out
  }
  $Actual = (Get-FileHash -Path $Out -Algorithm $Algorithm).Hash
  if ($Actual.ToLowerInvariant() -ne $Hash.ToLowerInvariant()) {
    Remove-Item -Force $Out
    throw "Checksum mismatch for $Out"
  }
}
if (!(Test-Path -LiteralPath $SourceApk)) {
  throw "Missing original APK: $SourceApk . Put it next to this script."
}
if (!(Test-Path (Join-Path $Work "VTUAN_GATE.apk"))) {
  throw "Missing VTUAN_GATE.apk from the ZIP kit."
}
if (!(Test-Path (Join-Path $Work "patch_host.py"))) {
  throw "Missing patch_host.py from the ZIP kit."
}
$Python = "python"
$py = Get-Command py -ErrorAction SilentlyContinue
if ($py) { $Python = "py" }
elseif (!(Get-Command python -ErrorAction SilentlyContinue)) {
  throw "Python 3 is required. Install with winget install Python.Python.3.12"
}
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
  throw "Java 17 is required. Install with winget install EclipseAdoptium.Temurin.17.JDK"
}
if (!(Get-Command keytool -ErrorAction SilentlyContinue)) {
  throw "JDK keytool is required; ensure JAVA_HOME/bin is in PATH."
}
$Apktool = Join-Path $Work "apktool_2.11.1.jar"
GetVerified "https://github.com/iBotPeaches/Apktool/releases/download/v2.11.1/apktool_2.11.1.jar" $Apktool "56d59c524fc764263ba8d345754d8daf55b1887818b15cd3b594f555d249e2db" "SHA256"
$BuildRoot = Join-Path $Work "build-tools"
$Zipalign = Join-Path $BuildRoot "zipalign.exe"
$Apksigner = Join-Path $BuildRoot "apksigner.bat"
if (!(Test-Path $Zipalign) -or !(Test-Path $Apksigner)) {
  $ZipFile = Join-Path $Work "build-tools_r35_windows.zip"
  GetVerified "https://dl.google.com/android/repository/build-tools_r35_windows.zip" $ZipFile "af059bb67cf7786f45ee0db85e2d24985df1b4b6" "SHA1"
  $Unpack = Join-Path $Work "sdk-build-tools-temp"
  if (Test-Path $Unpack) { Remove-Item -Recurse -Force $Unpack }
  Expand-Archive -Path $ZipFile -DestinationPath $Unpack -Force
  $ZipalignCandidate = Get-ChildItem -Path $Unpack -Filter zipalign.exe -Recurse | Select-Object -First 1
  if (!$ZipalignCandidate) { throw "zipalign.exe absent in downloaded Android build-tools" }
  if (Test-Path $BuildRoot) { Remove-Item -Recurse -Force $BuildRoot }
  Move-Item -Path $ZipalignCandidate.DirectoryName -Destination $BuildRoot
  Remove-Item -Recurse -Force $Unpack
}
$Output = Join-Path $Work "TTS_TOOL_V12.1_VTUAN_KEY_PATCHED.apk"
$KeyStore = Join-Path $Work "vtuan-test.jks"
$argsPy = @("patch_host.py", "--apk", $SourceApk, "--gate", (Join-Path $Work "VTUAN_GATE.apk"), "--apktool", $Apktool, "--output", $Output, "--keystore", $KeyStore, "--apksigner", $Apksigner, "--zipalign", $Zipalign)
if ($Python -eq "py") { $argsPy = @("-3") + $argsPy }
Write-Host "Building the patched APK..."
Push-Location $Work
try {
  MustRun $Python $argsPy
} finally {
  Pop-Location
}
Write-Host ""
Write-Host "FINISHED: $Output"
Write-Host "A patched APK is re-signed; back up original app data first."
