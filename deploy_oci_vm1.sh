#!/usr/bin/env bash
# =============================================================================
# SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
# OCI VM 1 DEPLOYMENT & EXECUTION SCRIPT
# Compatible with: Oracle Linux 8/9, RHEL, AlmaLinux, Ubuntu 22.04/24.04
# =============================================================================

set -e

echo "========================================================================="
echo "   SMART CITY TRAFFIC ENGINE - OCI VM 1 LAUNCHER"
echo "========================================================================="

# 1. Verify / Install Java 21
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q '21'; then
    echo "[*] Java 21 not detected. Attempting to install OpenJDK 21..."
    if command -v dnf &> /dev/null; then
        sudo dnf install -y java-21-openjdk java-21-openjdk-devel
    elif command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y openjdk-21-jdk
    else
        echo "[-] Package manager not recognized. Please install OpenJDK 21 manually."
        exit 1
    fi
fi

echo "[+] Java Version:"
java -version

# 2. Database Environment Variables
# If not already exported, prompt or use defaults
if [ -z "$DB_URL" ]; then
    echo ""
    echo "Enter the OCI MySQL Database Host / IP (e.g. OCI VM 2 Private IP or MDS endpoint):"
    read -r OCI_DB_HOST
    OCI_DB_HOST=${OCI_DB_HOST:-"127.0.0.1"}
    export DB_URL="jdbc:mysql://${OCI_DB_HOST}:3306/smart_traffic?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
fi

if [ -z "$DB_USER" ]; then
    echo "Enter Database Username (default: root):"
    read -r INPUT_USER
    export DB_USER=${INPUT_USER:-"root"}
fi

if [ -z "$DB_PASSWORD" ]; then
    echo -n "Enter Database Password: "
    read -rs INPUT_PASS
    echo ""
    export DB_PASSWORD="$INPUT_PASS"
fi

echo ""
echo "[+] Configured OCI Environment Variables:"
echo "    DB_URL      : $DB_URL"
echo "    DB_USER     : $DB_USER"
echo "    DB_PASSWORD : [PROTECTED]"

# 3. Locate Application JAR
JAR_FILE="SmartTrafficRouter-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    if [ -f "target/$JAR_FILE" ]; then
        JAR_FILE="target/$JAR_FILE"
    else
        echo "[-] Error: $JAR_FILE not found in current directory."
        exit 1
    fi
fi

# Ensure output directory exists
mkdir -p output

# 4. Execute Traffic Engine
echo ""
echo "========================================================================="
echo "   LAUNCHING SMART CITY TRAFFIC ENGINE ON OCI VM 1"
echo "========================================================================="

if [ "$1" == "--auto" ]; then
    java -jar "$JAR_FILE" --auto
elif [ "$1" == "--daemon" ]; then
    echo "[*] Starting in background daemon mode (logging to engine.log)..."
    nohup java -jar "$JAR_FILE" --auto > engine.log 2>&1 &
    echo "[+] Engine running with PID: $!"
    echo "    View logs using: tail -f engine.log"
else
    java -jar "$JAR_FILE"
fi
