@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
echo =============================================
echo   VTUAN - APK Key Server Patcher (Windows)
echo =============================================
echo.
echo Put the APK next to this .cmd and the .ps1 file.
echo.
set "OLD="
set /p "OLD=Old key-server base URL (leave empty for scan only): "
echo.
if "%OLD%"=="" (
 powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0VTUAN_GAN_SERVER_KEY.ps1"
) else (
 powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0VTUAN_GAN_SERVER_KEY.ps1" -OldBaseUrl "%OLD%"
)
echo.
echo See VTUAN_KEY_WORK\VTUAN_KET_QUA.txt for the report.
pause
