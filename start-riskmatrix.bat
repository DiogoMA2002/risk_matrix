@echo off
REM ============================================================
REM  Risk Matrix - one-click launcher (Windows)
REM  Double-click this file. It opens ONE control window.
REM  Closing that window shuts the whole app down.
REM ============================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-riskmatrix.ps1"
echo.
echo (This window can be closed.)
pause
