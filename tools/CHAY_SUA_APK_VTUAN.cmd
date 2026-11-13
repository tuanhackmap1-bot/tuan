@echo off
chcp 65001 >nul
echo.
echo VTUAN: Sua loi resources.arsc tren Android 11+
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0VTUAN_SUA_LOI_CAI_APK.ps1"
echo.
echo Kiem tra file VTUAN_HMLQ_V110_KEY_GATE_INSTALL_FIX.apk trong Downloads.
pause
