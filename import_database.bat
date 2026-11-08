@echo off
echo ===================================================
echo Smart City Traffic Router - MySQL Schema Importer
echo ===================================================
echo.
set /p MYSQL_USER=Enter MySQL Username (default root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo Importing smart_traffic_schema.sql into MySQL...
mysql -u %MYSQL_USER% -p < smart_traffic_schema.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Database and all 4 tables created successfully!
    echo Tables: emergency_vehicles, intersections, preemption_logs, corridor_reports
) else (
    echo.
    echo [ERROR] MySQL import failed. Please verify your password and MySQL path.
)
pause
