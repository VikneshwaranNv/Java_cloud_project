# Student 2 (Database Host & Cloud Infrastructure) — Complete Guide & Master Prompt

---

## 1. What Student 1 Gives to Student 2
Student 1 should copy or send these files to Student 2's laptop (via USB, Git repository, or WhatsApp/Email):
1. `smart_traffic_schema.sql` (The 4-table database schema).
2. The Git repo or project folder containing the Java source code.

---

## 2. Master Prompt to Copy-Paste into Antigravity on Student 2's Laptop

Copy and paste this exact prompt into the chat in VS Code with Antigravity on **Student 2's Laptop**:

```markdown
# MASTER PROMPT — STUDENT 2: DATABASE HOST & CLOUD NETWORKING INTEGRATION

Act as a senior database administrator, cloud network engineer, and project mentor.
My team project is: "Smart City Traffic Signal & Emergency Vehicle Green Corridor Router".

## Team Division of Responsibility:
- Student 1 (Application Host): Engineered the Java 21 Traffic Engine on OCI VM 1 (multithreaded signal preemption, String parsing, and corridor manifests).
- Student 2 (My Role - Database Host): Deploy the Traffic Network DB on OCI VM 2, configure OCI VCN Ingress rules, and configure OS firewall settings for incoming remote connections on port 3306.

## My Deliverables to Complete Today:
1. Deploy MySQL Community Server 8.4 on OCI VM 2 (or my laptop acting as Database Host).
2. Execute the schema script `smart_traffic_schema.sql` to create the 4 required tables:
   - `emergency_vehicles`
   - `intersections`
   - `preemption_logs`
   - `corridor_reports`
3. Configure OCI VCN Ingress security rules (or network firewall) allowing incoming TCP traffic on Port 3306 from Student 1's Application Host.
4. Configure Linux OS Firewall (`firewalld` / `ufw`) on VM 2 to permit port 3306.
5. Create a remote database user (`traffic_app` with strong password) with remote `@'%'` access privileges to `smart_traffic`.
6. Provide verification commands and evidence for the college submission:
   - OCI VCN Security List / Ingress rule verification.
   - OS firewall rule verification (`firewall-cmd --list-ports` or PowerShell firewall rule).
   - Network connectivity verification log (port 3306 listening and accepting remote connections).
   - SQL queries confirming incoming data from Student 1.

Please inspect my environment, guide me step-by-step through setting up the database, configuring the firewall, and verifying remote connectivity with Student 1.
```

---

## 3. Step-by-Step Actions: Student 1 vs. Student 2

```
+-------------------------------------------------------+       +-------------------------------------------------------+
|             STUDENT 1 (Laptop 1 / VM 1)               |       |             STUDENT 2 (Laptop 2 / VM 2)               |
|                  Application Host                     |       |                    Database Host                      |
+-------------------------------------------------------+       +-------------------------------------------------------+
| 1. Runs Java 21 Traffic Engine (MainApp).             |       | 1. Runs MySQL Server 8.4.                             |
| 2. Parses GPS CSV & validates Beacon regex.           |       | 2. Executes smart_traffic_schema.sql (4 tables).      |
| 3. Dispatches multi-threaded CorridorWorker threads.  |       | 3. Opens OS Firewall (Port 3306 TCP).                 |
| 4. Points DB_URL to Student 2's IP address.           |       | 4. Sets OCI VCN Ingress Rule for Port 3306.           |
| 5. Transmits corridor manifests & preemption logs     | ====> | 5. Creates remote user: traffic_app@'%'.              |
|    across network to Student 2's database.            | (3306)| 6. Receives & stores live records in MySQL.           |
+-------------------------------------------------------+       +-------------------------------------------------------+
```

---

## 4. Student 2 Technical Configuration Steps

### Step 2.1: Deploy MySQL 8.4
- **On OCI VM 2 (Linux):**
  ```bash
  sudo dnf install -y mysql-server
  sudo systemctl enable --now mysqld
  sudo systemctl status mysqld
  ```
- **On Student 2's Laptop (Windows):**
  - Install MySQL Community Server 8.4 and verify it is running on port 3306.

---

### Step 2.2: Configure OS Firewall for Remote Port 3306
- **On Linux (OCI VM 2):**
  ```bash
  sudo firewall-cmd --permanent --zone=public --add-port=3306/tcp
  sudo firewall-cmd --reload
  sudo firewall-cmd --list-ports
  ```
- **On Windows (Student 2's Laptop over Wi-Fi/LAN):**
  Run PowerShell as Administrator:
  ```powershell
  New-NetFirewallRule -DisplayName "MySQL Remote 3306" -Direction Inbound -LocalPort 3306 -Protocol TCP -Action Allow
  ```

---

### Step 2.3: Configure OCI VCN Ingress Security Rule (If using OCI Cloud)
In the Oracle Cloud Console:
1. Go to **Networking** $\rightarrow$ **Virtual Cloud Networks** $\rightarrow$ Select your VCN.
2. Click **Security Lists** $\rightarrow$ Select **Default Security List**.
3. Click **Add Ingress Rules**:
   - **Source Type:** CIDR
   - **Source CIDR:** `0.0.0.0/0` (or Student 1's VM 1 IP address)
   - **IP Protocol:** TCP
   - **Destination Port Range:** `3306`
   - **Description:** `Allow MySQL traffic from Student 1 Application Host`
4. Click **Add Ingress Rules**.

---

### Step 2.4: Execute Database Schema & Create Remote User
Inside MySQL on VM 2 (run as root):
```sql
-- 1. Create Database and Tables
SOURCE /path/to/smart_traffic_schema.sql;

-- 2. Create Remote User for Student 1
CREATE USER 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
FLUSH PRIVILEGES;

-- 3. Verify
SELECT user, host FROM mysql.user WHERE user='traffic_app';
SHOW TABLES FROM smart_traffic;
```

---

### Step 2.5: Test Connection from Student 1 to Student 2
On **Student 1's machine**, run in Command Prompt or PowerShell:
```cmd
set DB_URL=jdbc:mysql://<STUDENT_2_IP>:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USER=traffic_app
set DB_PASSWORD=SmartCity2026!

java -jar target\SmartTrafficRouter-1.0.0.jar --auto
```

Student 1's engine will connect, run the multithreaded simulation, and persist all records into Student 2's database!

---

## 5. College Submission Deliverables Checklist

| Deliverable | File / Evidence | Prepared By |
| :--- | :--- | :--- |
| **1. Database Schema** | `smart_traffic_schema.sql` (Tables: `emergency_vehicles`, `intersections`, `preemption_logs`, `corridor_reports`). | Prepared & verified |
| **2. Java Source Files** | `DBConnection.java`, `TrafficDAO.java`, `CorridorWorker.java`, `PreemptionWriter.java`, `MainApp.java`. | Prepared & verified |
| **3. Architecture Verification** | OCI Security List screenshot (Port 3306 ingress), OS firewall verification (`firewall-cmd --list-ports`), network trace log (`Test-NetConnection -Port 3306`). | Student 2 generates |
| **4. Execution Proofs** | CSV GPS parsing log, Beacon regex check (`EMG-[A-Z]{3}-\d{4}`), parallel worker signal logs, exported `.txt` performance summaries in `output/`. | Generated in `output/` |
