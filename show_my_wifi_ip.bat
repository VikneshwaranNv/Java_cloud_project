@echo off
title Student 2 - Wi-Fi / Mobile Hotspot IP Detector
cls
echo =========================================================================
echo    STUDENT 2 (DATABASE HOST) - WI-FI / MOBILE HOTSPOT IP DETECTOR
echo =========================================================================
echo.
echo Detecting your active Wi-Fi / Hotspot IPv4 address...
echo.

powershell -NoProfile -Command "Get-NetIPAddress -AddressFamily IPv4 | Where-Object { ($_.InterfaceAlias -match 'Wi-Fi|Wireless|WLAN|Local Area Connection|Ethernet') -and ($_.IPAddress -notmatch '^127\.|^169\.254\.') } | ForEach-Object { Write-Host '  [+] Active Interface: ' $_.InterfaceAlias; Write-Host '  [+] YOUR IP ADDRESS : ' $_.IPAddress -ForegroundColor Green; Write-Host '' }"

echo =========================================================================
echo Tell Student 1 to enter this IP in Main Menu Option [8]!
echo Or Student 1 can run: connect_wifi_student2.bat
echo.
echo Make sure MySQL is running on your laptop (Port 3306).
echo =========================================================================
pause
