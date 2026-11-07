<#
.SYNOPSIS
    Configures database environment variables for Smart City Traffic Router (Local or OCI Cloud).

.DESCRIPTION
    Sets $env:DB_URL, $env:DB_USER, and $env:DB_PASSWORD for the current PowerShell session,
    with an option to persist them permanently to User Environment Variables.

.PARAMETER OciHost
    IP address or hostname of the MySQL server (defaults to localhost).

.PARAMETER DbUser
    MySQL username (defaults to root).

.PARAMETER DbPassword
    MySQL password.

.PARAMETER Permanent
    If specified, saves the variables to User environment variables permanently.

.EXAMPLE
    .\set_env.ps1 -OciHost "129.153.xx.xx" -DbUser "admin" -DbPassword "SecretPass123"
    .\set_env.ps1 -Permanent
#>

param(
    [string]$OciHost,
    [string]$DbUser,
    [string]$DbPassword,
    [switch]$Permanent
)

Clear-Host
Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host "   SMART CITY TRAFFIC ROUTER - DATABASE CONFIGURATION HELPER" -ForegroundColor Cyan
Write-Host "   Target: Local MySQL 8.4 LTS or Oracle Cloud Infrastructure (OCI)" -ForegroundColor Cyan
Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host ""

# Prompt interactively if parameters not provided on command line
if ([string]::IsNullOrWhiteSpace($OciHost)) {
    $inputHost = Read-Host "Enter MySQL Host/IP [Press Enter for 'localhost']"
    if ([string]::IsNullOrWhiteSpace($inputHost)) {
        $OciHost = "localhost"
    } else {
        $OciHost = $inputHost.Trim()
    }
}

if ([string]::IsNullOrWhiteSpace($DbUser)) {
    $inputUser = Read-Host "Enter Database Username [Press Enter for 'root']"
    if ([string]::IsNullOrWhiteSpace($inputUser)) {
        $DbUser = "root"
    } else {
        $DbUser = $inputUser.Trim()
    }
}

if ($PSBoundParameters.ContainsKey('DbPassword') -eq $false -and [string]::IsNullOrEmpty($DbPassword)) {
    $securePass = Read-Host "Enter Database Password (press Enter if blank)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePass)
    $DbPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

# Construct JDBC URL based on host type
$useSsl = if ($OciHost -eq "localhost" -or $OciHost -eq "127.0.0.1") { "false" } else { "true" }
$jdbcUrl = "jdbc:mysql://${OciHost}:3306/smart_traffic?useSSL=${useSsl}&allowPublicKeyRetrieval=true&serverTimezone=UTC"

# 1. Apply to Current PowerShell Session
$env:DB_URL = $jdbcUrl
$env:DB_USER = $DbUser
$env:DB_PASSWORD = $DbPassword

Write-Host ""
Write-Host "[+] Environment variables applied to current PowerShell session:" -ForegroundColor Green
Write-Host "    `$env:DB_URL      = '$env:DB_URL'" -ForegroundColor Yellow
Write-Host "    `$env:DB_USER     = '$env:DB_USER'" -ForegroundColor Yellow
$maskedPass = if ([string]::IsNullOrEmpty($env:DB_PASSWORD)) { "<EMPTY>" } else { "********" }
Write-Host "    `$env:DB_PASSWORD = '$maskedPass'" -ForegroundColor Yellow

# 2. Persist permanently if requested
if ($Permanent) {
    [System.Environment]::SetEnvironmentVariable("DB_URL", $jdbcUrl, [System.EnvironmentVariableTarget]::User)
    [System.Environment]::SetEnvironmentVariable("DB_USER", $DbUser, [System.EnvironmentVariableTarget]::User)
    [System.Environment]::SetEnvironmentVariable("DB_PASSWORD", $DbPassword, [System.EnvironmentVariableTarget]::User)
    Write-Host "[+] Permanently saved to Windows User Environment Variables!" -ForegroundColor Green
}

Write-Host ""
Write-Host "To test connection now, run:" -ForegroundColor Cyan
Write-Host "    java -jar target/SmartTrafficRouter-1.0.0.jar" -ForegroundColor White
Write-Host "    (Select Option 1 to verify connectivity)" -ForegroundColor White
Write-Host "=========================================================================" -ForegroundColor Cyan
