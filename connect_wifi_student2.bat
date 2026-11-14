@echo off
title Student 1 - Connect to Student 2 over Wi-Fi / Hotspot
cls
echo =========================================================================
echo    STUDENT 1 (APP HOST) - CONNECT TO STUDENT 2'S DATABASE OVER WI-FI
echo =========================================================================
echo.
echo Step 1: Connect your laptop to the SAME Wi-Fi or Mobile Hotspot as Student 2.
echo Step 2: Ask Student 2 for their IPv4 address (from show_my_wifi_ip.bat).
echo.
set /p S2_IP=Enter Student 2's Wi-Fi IP address (e.g. 192.168.43.15): 

if "%S2_IP%"=="" (
    echo [!] No IP entered. Exiting.
    pause
    exit /b 1
)

set "DB_URL=jdbc:mysql://%S2_IP%:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000"
set "DB_USER=traffic_app"
set "DB_PASSWORD=SmartCity2026!"

echo.
echo Configured Database Target: %DB_URL%
echo Launching Smart City Traffic Engine...
echo.

cd /d "F:\Team Java"
if exist "target\SmartTrafficRouter-1.0.0.jar" (
    java -jar target\SmartTrafficRouter-1.0.0.jar
) else (
    echo [!] JAR not found. Running Maven build...
    call mvn package -DskipTests
    java -jar target\SmartTrafficRouter-1.0.0.jar
)
