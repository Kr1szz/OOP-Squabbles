@echo off
echo Killing running Java processes...
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM javaw.exe /T 2>nul
powershell -Command "Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force"
powershell -Command "Get-Process javaw -ErrorAction SilentlyContinue | Stop-Process -Force"
timeout /t 2 /nobreak >nul
call mvn clean package
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b %errorlevel%
)
java -jar target/matcher-v2.jar
pause
