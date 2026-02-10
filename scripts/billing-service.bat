@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0..\billing-service"
if errorlevel 1 (
    echo Error: No se pudo cambiar a la carpeta billing-service
    pause
    exit /b 1
)
mvn spring-boot:run
pause