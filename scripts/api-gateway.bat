@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0..\api-gateway"
if errorlevel 1 (
    echo Error: No se pudo cambiar a la carpeta api-gateway
    pause
    exit /b 1
)
mvn spring-boot:run
pause