#Requires -Version 5.1
<#
 Repair the Android 11+ APK install error:
  "Targeting R+ requires resources.arsc to be stored uncompressed and 4-byte aligned"
 Does NOT alter the app's authentication/menu code.
 Run with Windows PowerShell:
 powershell -NoProfile -ExecutionPolicy Bypass -File .\VTUAN_SUA_LOI_CAI_APK.ps1
#>
[CmdletBinding()]
param(
    [string]$InputApk = "$env:USERPROFILE\Downloads\VTUAN_HMLQ_V110_KEY_GATE_TEST.apk",
    [string]$OutputApk = "$env:USERPROFILE\Downloads\VTUAN_HMLQ_V110_KEY_GATE_INSTALL_FIX.apk"
)
$ErrorActionPreference="Stop"
$ProgressPreference="SilentlyContinue"
$global:LASTEXITCODE=0
[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
$Root=Join-Path $env:USERPROFILE "Downloads\VTUAN_ARSC_REPAIR"
$SignedDir=Join-Path $Root "signed"
$RawApk=Join-Path $Root "VTUAN_UNALIGNED_UNSIGNED.apk"
$JavaSource=Join-Path $Root "FixArsc.java"
$Jar=Join-Path $Root "uber-apk-signer.jar"
$LogFile=Join-Path $Root "repair.log"
New-Item -ItemType Directory -Path $Root,$SignedDir -Force | Out-Null
if(Test-Path $LogFile){Remove-Item $LogFile -Force}
function Log([string]$s){Write-Host $s; Add-Content -LiteralPath $LogFile -Value $s -Encoding UTF8}
function VerifyExit([string]$action){if($LASTEXITCODE -ne 0){throw "$action failed with code $LASTEXITCODE"}}
try{
    if(!(Test-Path -LiteralPath $InputApk -PathType Leaf)){ throw "Missing input APK: $InputApk" }
    if([System.IO.Path]::GetFullPath($InputApk) -eq [System.IO.Path]::GetFullPath($OutputApk)){throw "Input and output cannot be the same APK"}
    $inputSize=(Get-Item -LiteralPath $InputApk).Length
    if($inputSize -lt 1048576){throw "Input APK is too small ($inputSize bytes); do not edit it"}
    Log "Input: $InputApk"
    Log "Size: $inputSize bytes"
    if(!(Get-Command java -ErrorAction SilentlyContinue)){throw "Java JDK 17 is required. Install via: winget install --id EclipseAdoptium.Temurin.17.JDK -e"}
    if(!(Get-Command javac -ErrorAction SilentlyContinue)){throw "JDK compiler javac.exe not found. Install JDK 17: winget install --id EclipseAdoptium.Temurin.17.JDK -e"}
    $JavaUri="https://raw.githubusercontent.com/tuanhackmap1-bot/tuan/vtuan-apk-key-tool-20261113/tools/FixArsc.java"
    Log "Downloading source from VTUAN GitHub..."
    Invoke-WebRequest -Uri $JavaUri -UseBasicParsing -OutFile $JavaSource
    if((Get-Item -LiteralPath $JavaSource).Length -lt 1000){throw "Source download failed (too small)"}
    Log "Compiling repair helper..."
    & javac -encoding UTF-8 -d $Root $JavaSource
    VerifyExit "javac"
    if(Test-Path -LiteralPath $RawApk){Remove-Item -LiteralPath $RawApk -Force}
    Log "Repairing resources.arsc (streaming, no extraction)..."
    & java -Xmx1024m -cp $Root FixArsc $InputApk $RawApk
    VerifyExit "FixArsc"
    if(!(Test-Path -LiteralPath $Jar)){
        $h=@{"User-Agent"="VTUAN-ARSC-Repair";"Accept"="application/vnd.github+json"}
        $release=Invoke-RestMethod -Uri "https://api.github.com/repos/patrickfav/uber-apk-signer/releases/latest" -Headers $h
        $asset=@($release.assets | Where-Object {$_.name -match '^uber-apk-signer.*\.jar$'} | Select-Object -First 1)
        if($asset.Count -eq 0){throw "Unable to find uber-apk-signer JAR in official release"}
        Log "Downloading official signer: $($asset[0].name)"
        Invoke-WebRequest -Uri $asset[0].browser_download_url -Headers $h -OutFile $Jar -UseBasicParsing
        if((Get-Item -LiteralPath $Jar).Length -lt 100000){throw "Signer download too small"}
    }
    Get-ChildItem -LiteralPath $SignedDir -Filter "*.apk" -File | Remove-Item -Force
    Log "Aligning and signing APK..."
    & java -jar $Jar -a $RawApk --out $SignedDir --allowResign
    VerifyExit "Signer"
    $apks=@(Get-ChildItem -LiteralPath $SignedDir -Filter "*.apk" -File | Sort-Object LastWriteTime -Descending)
    if($apks.Count -ne 1){throw "Expected exactly 1 signed APK; found $($apks.Count)"}
    Log "Verifying resources.arsc alignment..."
    & java -cp $Root FixArsc --verify $apks[0].FullName
    VerifyExit "resources.arsc verification"
    if(Test-Path -LiteralPath $OutputApk){Remove-Item -LiteralPath $OutputApk -Force}
    Copy-Item -LiteralPath $apks[0].FullName -Destination $OutputApk -Force
    Log "SUCCESS: $OutputApk"
    Log "Output bytes: $((Get-Item -LiteralPath $OutputApk).Length)"
    Log "SHA256: $((Get-FileHash -LiteralPath $OutputApk -Algorithm SHA256).Hash)"
    Log "NOTE: This repair only addresses the resources.arsc compression/alignment error. It does not prove the app runs or that authentication works."
    Log "NOTE: This APK is re-signed, so it may conflict with an existing package signed with a different certificate."
} catch {
    Log "FAILED: $($_.Exception.Message)"
    Log "Detailed report: $LogFile"
    exit 1
}
