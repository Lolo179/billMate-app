@echo off
setlocal enabledelayedexpansion
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0..\billing-service"
if errorlevel 1 (
    echo Error: No se pudo cambiar a la carpeta billing-service
    pause
    exit /b 1
)
mvn spring-boot:run
pause