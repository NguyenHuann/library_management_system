@echo off
setlocal
cd /d "%~dp0"
if not exist "target\library-management.jar" (
    echo Build first using build.cmd or mvn clean verify.
    pause
    exit /b 1
)
java -jar "target\library-management.jar" --client %*
