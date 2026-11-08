@echo off
REM ==============================================================================
REM SMART CITY TRAFFIC ROUTER - DATABASE TEST & VERIFICATION UTILITY
REM ==============================================================================
echo ==========================================================
echo  Smart City Traffic Router - Database Verification Test
echo ==========================================================
echo.

set /p DB_HOST=Enter Database Host (default 127.0.0.1): 
if "%DB_HOST%"=="" set DB_HOST=127.0.0.1

set /p DB_USER=Enter Database User (default traffic_app): 
if "%DB_USER%"=="" set DB_USER=traffic_app

echo.
echo Connecting to %DB_HOST%:3306 as %DB_USER% and executing verify_database.sql...
mysql -h %DB_HOST% -P 3306 -u %DB_USER% -p < verify_database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================================
    echo  [SUCCESS] Database connection and query checks PASSED!
    echo ==========================================================
) else (
    echo.
    echo [ERROR] Verification failed. Check network, firewall, or user credentials.
)
pause
