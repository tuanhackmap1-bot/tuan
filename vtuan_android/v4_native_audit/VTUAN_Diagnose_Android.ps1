# Read-only Android native-crash diagnostics for VTUAN V4.
# Usage: powershell -ExecutionPolicy Bypass -File .\VTUAN_Diagnose_Android.ps1
# Does not install/uninstall packages, clear game data, alter files, or read keys.
param(
  [string]$Package = "com.garena.game.kgvn1",
  [string]$OutputFile = "VTUAN_DEVICE_NATIVE_REPORT.txt"
)
$ErrorActionPreference = 'Stop'
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
  Write-Error "Chua tim thay adb trong PATH. Hay mo PowerShell o thu muc platform-tools."
  exit 1
}
$devices = @(& adb devices | Where-Object { $_ -match '\tdevice\s*$' })
if ($devices.Count -ne 1) {
  Write-Error "Can dung 1 dien thoai Android da bat USB debugging (hien co $($devices.Count) thiet bi)."
  exit 1
}
$stamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
$abi = ((& adb shell getprop ro.product.cpu.abilist) -join '').Trim()
$androidVersion = ((& adb shell getprop ro.build.version.release) -join '').Trim()
$paths = @(& adb shell pm path $Package | Where-Object { $_ -match '^package:' })
$officialPaths = @(& adb shell pm path "com.garena.game.kgvn" | Where-Object { $_ -match '^package:' })
$dumpsys = @(& adb shell dumpsys package $Package 2>&1)
$pkgFacts = @($dumpsys | Select-String -Pattern 'versionName=|versionCode=|primaryCpuAbi=|secondaryCpuAbi=|splitNames=|sourceDir=' | Select-Object -First 30 | ForEach-Object { $_.Line.Trim() })
$crashBuffer = @(& adb logcat -d -b crash -t 1200 2>&1)
$crashFacts = @($crashBuffer | Select-String -Pattern 'UnsatisfiedLinkError|dlopen failed|libgcloud\.so|libil2cpp\.so|libysmteam\.so|FATAL EXCEPTION|NoClassDefFoundError' | Select-Object -Last 45 | ForEach-Object { $_.Line.Trim() })
$lines = @(
  "VTUAN Native Device Diagnostic"
  "Date: $stamp"
  "Package: $Package"
  "Android: $androidVersion"
  "ABI: $abi"
  "Installed APK / split APK paths:"
)
if ($paths.Count -eq 0) { $lines += "(Package not found on this device)" }
else { $lines += $paths }
$lines += ""
$lines += "Official Liên Quân package paths (com.garena.game.kgvn):"
if ($officialPaths.Count -eq 0) { $lines += "(Not installed or not visible)" }
else { $lines += $officialPaths }
$lines += ""
$lines += "Package metadata:"
if ($pkgFacts.Count -eq 0) { $lines += "(No package metadata found)" }
else { $lines += $pkgFacts }
$lines += ""
$lines += "Relevant crash evidence from Android crash buffer:"
if ($crashFacts.Count -eq 0) { $lines += "(No matching crash entries found)" }
else { $lines += $crashFacts }
$lines += ""
$lines += "LIMITS: pm path does not prove a .so is loaded. An installed split may supply libraries missing from base APK. No keys or application private data were read."
$fullPath = [System.IO.Path]::GetFullPath($OutputFile)
$lines | Out-File -FilePath $fullPath -Encoding UTF8
Write-Host "Da luu bao cao: $fullPath"
Write-Host "Bao cao chi doc, khong chinh sua du lieu game."
