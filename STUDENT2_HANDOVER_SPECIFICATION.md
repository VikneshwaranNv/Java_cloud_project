# Student 2 Handover Specification & Interface Contract
## Project: Smart City Traffic Signal & Emergency Vehicle Green Corridor Router
**Prepared by:** Student 1 (Application Host Engineer)  
**For:** Student 2 (Database & Cloud Infrastructure Host Engineer)  
**Scheduled Execution:** Tomorrow  

---

## 1. System Role Definition

| Role | Engineer | Host Machine | Core Responsibilities |
| :--- | :--- | :--- | :--- |
| **Application Host** | **Student 1** | OCI VM 1 (or Local Dev PC) | Runs Java 21 Traffic Engine, manages multi-threaded signal preemption, String parsing, and produces corridor manifests. |
| **Database Host** | **Student 2** | OCI VM 2 (or OCI MySQL MDS) | Provisions MySQL 8.4 Server, executes `smart_traffic_schema.sql`, configures OCI VCN Ingress Rules, and configures OS firewall for incoming port 3306 TCP traffic. |

---

## 2. Database Specification for Student 2

- **Database Name:** `smart_traffic`
- **Port:** `3306` (TCP)
- **Engine:** `InnoDB`
- **Character Set:** `utf8mb4` (Collation: `utf8mb4_unicode_ci`)
- **DDL Script to Execute:** [`smart_traffic_schema.sql`](smart_traffic_schema.sql)

### The 4 Tables to be Created:
1. `emergency_vehicles`: Stores vehicle telemetry, Beacon IDs, and phase metadata.
2. `intersections`: Stores signal states, coordinates, and camera vehicle density counts.
3. `preemption_logs`: Stores microsecond-level worker thread override events.
4. `corridor_reports`: Stores final transit duration, priority score, ETE, CMI, and assigned tier.

---

## 3. Step-by-Step Instructions for Student 2 (Tomorrow)

### Step 1: Install & Start MySQL on OCI VM 2
```bash
# On Oracle Linux / RHEL:
sudo dnf install -y mysql-server
sudo systemctl enable --now mysqld
sudo systemctl status mysqld
```

### Step 2: Configure OS Firewall on VM 2
Open port 3306 so VM 1 can communicate with VM 2:
```bash
sudo firewall-cmd --permanent --zone=public --add-port=3306/tcp
sudo firewall-cmd --reload
```

### Step 3: Configure OCI Virtual Cloud Network (VCN) Ingress Rule
In the **Oracle Cloud Console**:
1. Navigate to **Networking** $\rightarrow$ **Virtual Cloud Networks**.
2. Click your VCN $\rightarrow$ Click **Security Lists** $\rightarrow$ Select the default security list.
3. Click **Add Ingress Rules**:
   - **Source Type:** CIDR
   - **Source CIDR:** `10.0.0.0/16` (or OCI VM 1 Private IP, e.g. `10.0.0.x/32`)
   - **IP Protocol:** TCP
   - **Destination Port Range:** `3306`
   - **Description:** Allow MySQL incoming traffic from VM 1 Application Host.
4. Click **Add Ingress Rules**.

### Step 4: Initialize Schema & Create Remote User
Inside MySQL on VM 2:
```sql
-- 1. Run smart_traffic_schema.sql
SOURCE /path/to/smart_traffic_schema.sql;

-- 2. Create remote user for Student 1 VM 1
CREATE USER 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
FLUSH PRIVILEGES;
```

---

## 4. How Student 1 Tests Connectivity to Student 2

Once Student 2 completes the steps above, Student 1 will run:

### From VM 1 (or Local PC):
```powershell
# Set Student 2's Database Details
$env:DB_URL = "jdbc:mysql://<STUDENT_2_VM2_IP>:3306/smart_traffic?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER = "traffic_app"
$env:DB_PASSWORD = "SmartCity2026!"

# Run Option 1 to verify connectivity
java -jar target/SmartTrafficRouter-1.0.0.jar
```

When Option 1 prints:
`[+] SUCCESS: Connected to MySQL database successfully!`

The multi-host distributed system is connected and verified.
