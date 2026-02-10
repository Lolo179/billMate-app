@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0..\frontend-service"
if errorlevel 1 (
    echo Error: No se pudo cambiar a la carpeta frontend-service
    pause
    exit /b 1
)
mvn spring-boot:run
pause
