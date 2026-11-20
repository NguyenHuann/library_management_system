@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    call mvn clean verify
) else if exist "D:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" (
    call "D:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" clean verify
) else (
    echo Maven was not found. Install Maven or build this Maven project in NetBeans.
    exit /b 1
)
exit /b %errorlevel%
