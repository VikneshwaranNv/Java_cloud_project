#!/usr/bin/env bash
# ==============================================================================
# SMART CITY TRAFFIC ROUTER - OCI VM 2 DATABASE HOST AUTOMATED DEPLOYMENT SCRIPT
# ==============================================================================
set -e

echo "=========================================================="
echo " Starting OCI VM 2 Database Host Automated Provisioning   "
echo "=========================================================="

# 1. Update packages and install MySQL Server
echo "[1/6] Installing MySQL Server..."
if command -v apt-get &> /dev/null; then
    sudo apt-get update -y
    sudo apt-get install -y mysql-server ufw
elif command -v dnf &> /dev/null; then
    sudo dnf update -y
    sudo dnf install -y mysql-server firewalld
    sudo systemctl enable --now firewalld
fi

# 2. Configure MySQL to listen on all network interfaces (0.0.0.0)
echo "[2/6] Configuring MySQL network binding to 0.0.0.0..."
CNF_FILES=(
    "/etc/mysql/mysql.conf.d/mysqld.cnf"
    "/etc/mysql/my.cnf"
    "/etc/my.cnf.d/mysql-server.cnf"
    "/etc/my.cnf"
)

for cnf in "${CNF_FILES[@]}"; do
    if [ -f "$cnf" ]; then
        echo "Updating $cnf to bind to 0.0.0.0..."
        sudo sed -i 's/^bind-address\s*=.*/bind-address = 0.0.0.0/' "$cnf" || true
        sudo sed -i 's/^mysqlx-bind-address\s*=.*/mysqlx-bind-address = 0.0.0.0/' "$cnf" || true
    fi
done

sudo systemctl enable mysql || sudo systemctl enable mysqld
sudo systemctl restart mysql || sudo systemctl restart mysqld

# 3. Configure OS Firewall for Port 3306
echo "[3/6] Opening Port 3306 in OS Firewall..."
if command -v firewall-cmd &> /dev/null && sudo systemctl is-active --quiet firewalld; then
    sudo firewall-cmd --permanent --add-port=3306/tcp
    sudo firewall-cmd --reload
    echo "firewalld: 3306/tcp allowed."
elif command -v ufw &> /dev/null; then
    sudo ufw allow 3306/tcp
    echo "ufw: 3306/tcp allowed."
fi

# Also ensure iptables allows port 3306
if command -v iptables &> /dev/null; then
    sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 3306 -j ACCEPT || true
fi

# 4. Import Database Schema & Seeds
echo "[4/6] Executing smart_traffic_schema.sql and seed_intersections.sql..."
sudo mysql < smart_traffic_schema.sql
sudo mysql < seed_intersections.sql

# 5. Create remote database user traffic_app@'%'
echo "[5/6] Creating remote user traffic_app@'%' with full privileges on smart_traffic..."
sudo mysql -e "
CREATE USER IF NOT EXISTS 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
FLUSH PRIVILEGES;
"

# 6. Verification
echo "[6/6] Verifying Network Listener and Tables..."
echo "Network Listener:"
sudo ss -tlpn | grep 3306 || true

echo "Table Row Counts:"
sudo mysql -e "USE smart_traffic; SELECT (SELECT COUNT(*) FROM emergency_vehicles) AS vehicles, (SELECT COUNT(*) FROM intersections) AS intersections;"

PUBLIC_IP=$(curl -s https://api.ipify.org || echo "<YOUR_VM2_PUBLIC_IP>")
echo "=========================================================="
echo " [SUCCESS] OCI VM 2 Database Host is READY!              "
echo "=========================================================="
echo "Share these connection details with Student 1:"
echo "Host IP:    $PUBLIC_IP"
echo "Port:       3306"
echo "Database:   smart_traffic"
echo "Username:   traffic_app"
echo "Password:   SmartCity2026!"
echo "JDBC URL:   jdbc:mysql://$PUBLIC_IP:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
echo "=========================================================="
