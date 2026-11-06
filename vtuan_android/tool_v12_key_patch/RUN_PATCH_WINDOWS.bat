@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0patch_on_windows.ps1"
if errorlevel 1 echo BUILD FAILED - read error above
pause
