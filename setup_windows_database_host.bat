@echo off
REM ==============================================================================
REM SMART CITY TRAFFIC ROUTER - WINDOWS DATABASE HOST AUTOMATED SETUP SCRIPT
REM ==============================================================================
echo ==========================================================
echo  Starting Windows Database Host Automated Configuration
echo ==========================================================
echo.

REM 1. Open Windows Defender Firewall Port 3306
echo [1/4] Configuring Windows Defender Firewall for TCP Port 3306...
netsh advfirewall firewall show rule name="MySQL Port 3306 Remote" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Firewall rule already exists.
) else (
    netsh advfirewall firewall add rule name="MySQL Port 3306 Remote" dir=in action=allow protocol=TCP localport=3306
    if %ERRORLEVEL% EQU 0 (
        echo [OK] Inbound rule for Port 3306 added successfully!
    ) else (
        echo [WARNING] Administrator privileges recommended to modify firewall rules.
    )
)

echo.
REM 2. Prompt for MySQL root credentials
set /p MYSQL_USER=Enter MySQL Admin User (default root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo.
echo [2/4] Importing smart_traffic_schema.sql...
mysql -u %MYSQL_USER% -p < smart_traffic_schema.sql
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to import schema. Check MySQL user/password.
    pause
    exit /b 1
)

echo.
echo [3/4] Importing seed_intersections.sql...
mysql -u %MYSQL_USER% -p < seed_intersections.sql

echo.
echo [4/4] Creating remote user traffic_app@'%'...
mysql -u %MYSQL_USER% -p -e "CREATE USER IF NOT EXISTS 'traffic_app'@'%%' IDENTIFIED BY 'SmartCity2026!'; GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%%'; FLUSH PRIVILEGES;"

echo.
echo ==========================================================
echo  [SUCCESS] Windows Database Host is configured!
echo ==========================================================
echo Remote User: traffic_app
echo Password:    SmartCity2026!
echo Database:    smart_traffic
echo.
pause
