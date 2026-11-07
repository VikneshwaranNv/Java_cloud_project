@echo off
REM =========================================================================
REM   Smart City Traffic Router - Database Environment Setup (Batch / CMD)
REM =========================================================================

set /p OCI_HOST="Enter MySQL Host/IP (Press Enter for localhost): "
if "%OCI_HOST%"=="" set OCI_HOST=localhost

set /p OCI_USER="Enter Database Username (Press Enter for root): "
if "%OCI_USER%"=="" set OCI_USER=root

set /p OCI_PASS="Enter Database Password: "

if "%OCI_HOST%"=="localhost" (
    set DB_URL=jdbc:mysql://localhost:3306/smart_traffic?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
) else (
    set DB_URL=jdbc:mysql://%OCI_HOST%:3306/smart_traffic?useSSL=true^&serverTimezone=UTC
)

set DB_USER=%OCI_USER%
set DB_PASSWORD=%OCI_PASS%

echo.
echo [!] Configured Environment Variables for this CMD session:
echo     DB_URL=%DB_URL%
echo     DB_USER=%DB_USER%
echo.
echo Launching Application...
java -jar target\SmartTrafficRouter-1.0.0.jar
