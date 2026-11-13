#Requires -Version 5.1
<#
 VTUAN_GAN_SERVER_KEY.ps1
 Run on Windows PowerShell 5.1+:
 powershell -NoProfile -ExecutionPolicy Bypass -File .\VTUAN_GAN_SERVER_KEY.ps1
 Optional: -ApkPath "C:\path\app.apk" -OldBaseUrl "https://old-key-host.example"
 Purpose: inspect APK and replace a STATIC old server base URL in smali/XML/text,
          then rebuild and debug-sign with Apktool + Uber APK Signer.
 Limit: does NOT create a login UI or translate incompatible key protocols,
        decrypt obfuscated constants, or modify native .so authentication logic.
#>
[CmdletBinding()]
param(
    [string]$ApkPath = "",
    [string]$OldBaseUrl = "",
    [string]$NewBaseUrl = "https://vtuan-103-159-51-231.sslip.io",
    [switch]$ScanOnly
)
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$BaseDir = Split-Path -Parent $PSCommandPath
if (-not $BaseDir) { $BaseDir = (Get-Location).Path }
$ToolsDir = Join-Path $BaseDir "VTUAN_TOOLS"
$WorkDir = Join-Path $BaseDir "VTUAN_KEY_WORK"
$Decoded = Join-Path $WorkDir "decoded"
$Unsigned = Join-Path $WorkDir "VTUAN_UNSIGNED.apk"
$SignDir = Join-Path $WorkDir "signed"
$Output = Join-Path $BaseDir "VTUAN_KEY_SIGNED.apk"
$Report = Join-Path $WorkDir "VTUAN_KET_QUA.txt"
New-Item -ItemType Directory -Force -Path $ToolsDir,$WorkDir | Out-Null
if (Test-Path -LiteralPath $Report) { Remove-Item -LiteralPath $Report -Force }
function Log([string]$message) {
    Write-Host $message
    Add-Content -LiteralPath $Report -Value $message -Encoding UTF8
}
function CheckLastExit([string]$name) {
    if ($LASTEXITCODE -ne 0) { throw "$name failed. Exit code: $LASTEXITCODE" }
}
function GetLatestAsset([string]$repo,[string]$regex,[string]$save) {
    if (Test-Path -LiteralPath $save) { return $save }
    $headers = @{ "User-Agent" = "VTUAN-Windows-APK-Tool"; "Accept"="application/vnd.github+json" }
    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases/latest" -Headers $headers
    $asset = @($release.assets | Where-Object { $_.name -match $regex } | Select-Object -First 1)
    if ($asset.Count -eq 0) { throw "No release JAR matched: $repo ($regex)" }
    Log "Downloading official release asset: $($asset[0].name)"
    Invoke-WebRequest -Uri $asset[0].browser_download_url -Headers $headers -OutFile $save -UseBasicParsing
    if ((Get-Item -LiteralPath $save).Length -lt 100000) { throw "Download too small: $save" }
    return $save
}
function FindURLs([string]$s) {
    [regex]::Matches($s, 'https?://[^\s"''<>\\]{4,220}') |
        ForEach-Object { $_.Value.TrimEnd([char[]]@(')',',',';',']','}')) }
}
function ReadText([string]$path) {
    return [System.IO.File]::ReadAllText($path,[System.Text.Encoding]::UTF8)
}
function WriteText([string]$path,[string]$text) {
    [System.IO.File]::WriteAllText($path,$text,(New-Object System.Text.UTF8Encoding($false)))
}
try {
    if ([string]::IsNullOrWhiteSpace($ApkPath)) {
        $allApks = @(Get-ChildItem -LiteralPath $BaseDir -Filter *.apk -File)
        if ($allApks.Count -eq 0) { throw "No APK in $BaseDir. Copy original APK next to this script, or use -ApkPath." }
        if ($allApks.Count -gt 1) {
            Log "More than one APK found. Use -ApkPath with the original filename:"
            $allApks | ForEach-Object { Log "  $($_.FullName)" }
            throw "Multiple APKs: cannot choose safely"
        }
        $ApkPath = $allApks[0].FullName
    }
    $ApkPath = (Resolve-Path -LiteralPath $ApkPath).Path
    if ([System.IO.Path]::GetExtension($ApkPath).ToLowerInvariant() -ne ".apk") { throw "Input is not .apk" }
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw "Java not installed. Install JDK 17 (example: winget install EclipseAdoptium.Temurin.17.JDK), reopen PowerShell and retry."
    }
    Log "Input: $ApkPath"
    Log "Size: $([math]::Round((Get-Item -LiteralPath $ApkPath).Length / 1MB,2)) MB"
    Log "SHA256: $((Get-FileHash -LiteralPath $ApkPath -Algorithm SHA256).Hash)"
    Log "New host: $NewBaseUrl"
    $ApktoolJar = GetLatestAsset "iBotPeaches/Apktool" '^apktool[_-].*\.jar$' (Join-Path $ToolsDir "apktool.jar")
    if (Test-Path -LiteralPath $Decoded) { Remove-Item -LiteralPath $Decoded -Force -Recurse }
    Log "Decoding with Apktool ..."
    & java -jar $ApktoolJar d -f -o $Decoded $ApkPath
    CheckLastExit "Apktool decode"
    $Allowed = @(".smali",".xml",".json",".txt",".properties",".js",".html")
    $textFiles = @(Get-ChildItem -LiteralPath $Decoded -Recurse -File | Where-Object {
        $Allowed -contains $_.Extension.ToLowerInvariant() -and $_.Length -lt 6MB
    })
    $urlSet = New-Object "System.Collections.Generic.HashSet[string]" ([System.StringComparer]::OrdinalIgnoreCase)
    foreach ($f in $textFiles) {
        try {
            $t = ReadText $f.FullName
            foreach ($u in @(FindURLs $t)) {
                if ($u.Length -gt 7 -and $u.Length -lt 220) { [void]$urlSet.Add($u) }
            }
        } catch { Log "WARN: Cannot read $($f.FullName)" }
    }
    $nativeFiles = @(Get-ChildItem -LiteralPath (Join-Path $Decoded "lib") -Filter *.so -Recurse -File -ErrorAction SilentlyContinue)
    $nativeHits = 0
    foreach ($f in $nativeFiles) {
        if ($f.Length -gt 100MB) { Log "WARN: Skipped huge native library $($f.Name)"; continue }
        try {
            $bytes = [System.IO.File]::ReadAllBytes($f.FullName)
            $ascii = [System.Text.Encoding]::ASCII.GetString($bytes)
            foreach ($u in @(FindURLs $ascii)) {
                if ($u.Length -gt 7 -and $u.Length -lt 220) { [void]$urlSet.Add($u) }
            }
            if ($OldBaseUrl -and $ascii.Contains($OldBaseUrl)) {
                $nativeHits += 1
                Log "NATIVE MATCH: $($f.FullName)"
            }
        } catch { Log "WARN: Cannot scan native library $($f.Name)" }
    }
    Log ""
    Log "URLs found in decoded text/native libraries:"
    @($urlSet | Sort-Object | Select-Object -First 150) | ForEach-Object { Log "  $_" }
    Log ""
    if ($ScanOnly -or [string]::IsNullOrWhiteSpace($OldBaseUrl)) {
        Log "SCAN COMPLETE. No modifications were made."
        Log "To replace a verified OLD URL, rerun with -OldBaseUrl 'https://old-server.example'."
        Log "Only STATIC smali/text/XML strings can be replaced. A new login UI or protocol needs source code."
        exit 0
    }
    if ($OldBaseUrl -eq $NewBaseUrl) { throw "OldBaseUrl and NewBaseUrl are identical" }
    if ($nativeHits -gt 0) {
        throw "Old URL exists in native .so file(s). Automatic replacement is unsafe: no APK will be produced. The native code/API must be edited with its original source."
    }
    $changedFiles = 0
    $changedOccurrences = 0
    foreach ($f in $textFiles) {
        $t = ReadText $f.FullName
        if (-not $t.Contains($OldBaseUrl)) { continue }
        $count = ([regex]::Matches($t,[regex]::Escape($OldBaseUrl))).Count
        WriteText $f.FullName ($t.Replace($OldBaseUrl,$NewBaseUrl))
        $changedFiles += 1
        $changedOccurrences += $count
        Log "PATCHED $($f.FullName) ($count)"
    }
    Log "Changed $changedOccurrences strings in $changedFiles files."
    if ($changedOccurrences -eq 0) {
        throw "Old URL was not found in editable text. No APK created. Check the scan report; key logic may be native or obfuscated."
    }
    if (Test-Path -LiteralPath $Unsigned) { Remove-Item -LiteralPath $Unsigned -Force }
    Log "Building unsigned APK ..."
    & java -jar $ApktoolJar b $Decoded -o $Unsigned
    CheckLastExit "Apktool build"
    if (-not (Test-Path -LiteralPath $Unsigned)) { throw "Build produced no APK" }
    $SignerJar = GetLatestAsset "patrickfav/uber-apk-signer" '^uber-apk-signer.*\.jar$' (Join-Path $ToolsDir "uber-apk-signer.jar")
    if (Test-Path -LiteralPath $SignDir) { Remove-Item -LiteralPath $SignDir -Force -Recurse }
    New-Item -ItemType Directory -Force -Path $SignDir | Out-Null
    Log "Signing and verifying with a DEBUG signing key ..."
    & java -jar $SignerJar -a $Unsigned --out $SignDir --allowResign
    CheckLastExit "Uber APK Signer"
    $signed = @(Get-ChildItem -LiteralPath $SignDir -Filter "*.apk" -File | Sort-Object LastWriteTime -Descending)
    if ($signed.Count -eq 0) { throw "Signer produced no APK" }
    Copy-Item -LiteralPath $signed[0].FullName -Destination $Output -Force
    Log "DONE: $Output"
    Log "SHA256: $((Get-FileHash -LiteralPath $Output -Algorithm SHA256).Hash)"
    Log "IMPORTANT: This is only a host-string replacement. It does NOT verify that the VTUAN API uses the original app's request/response protocol."
    Log "IMPORTANT: Re-signing changes the app's certificate. It may not update an existing installation and may fail app integrity checks."
} catch {
    Log "ERROR: $($_.Exception.Message)"
    Log "Report: $Report"
    exit 1
}