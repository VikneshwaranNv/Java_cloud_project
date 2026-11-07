<#
.SYNOPSIS
    Convenience runner for Smart City Traffic Router.

.DESCRIPTION
    Sets local MySQL credentials and runs the application JAR.

.PARAMETER Auto
    If specified, runs the automated end-to-end simulation pipeline.
    Otherwise, starts the interactive menu.

.EXAMPLE
    .\run.ps1
    .\run.ps1 -Auto
#>

param(
    [switch]$Auto
)

# Set database environment variables (defaults to verified local credentials)
if ([string]::IsNullOrWhiteSpace($env:DB_URL)) {
    $env:DB_URL = "jdbc:mysql://127.0.0.1:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
}
if ([string]::IsNullOrWhiteSpace($env:DB_USER)) {
    $env:DB_USER = "root"
}
if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $env:DB_PASSWORD = "viknesh@07"
}

$jarPath = "target\SmartTrafficRouter-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[!] Standalone JAR not found. Building with Maven..." -ForegroundColor Yellow
    mvn clean package -DskipTests
}

if ($Auto) {
    Write-Host "[*] Launching Automated End-to-End Simulation Pipeline..." -ForegroundColor Cyan
    java -jar $jarPath --auto
} else {
    Write-Host "[*] Launching Interactive Console Menu..." -ForegroundColor Cyan
    java -jar $jarPath
}
