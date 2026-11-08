# Architecture Verification Document (AVD)
## Project Assignment Set 31: Smart City Traffic Signal & Emergency Vehicle Green Corridor Router
**Host Roles:**
- **Student 1 (Application Host):** Runs Java 21 Engine on OCI VM 1 / Local Node
- **Student 2 (Database Host):** Deploys MySQL 8.4 LTS on OCI VM 2 / Remote Host

---

## 1. Network Topology & Port Mapping

```
+------------------------------------+         Stateful Inbound TCP 3306         +-------------------------------------+
|        STUDENT 1 (APP HOST)        | ----------------------------------------> |         STUDENT 2 (DB HOST)         |
|  - OCI VM 1 (Ubuntu 22.04 LTS)     |                                           |  - OCI VM 2 (Oracle Linux 9/Ubuntu) |
|  - Java 21 LTS Engine (Maven)      |                                           |  - MySQL 8.4 LTS Community Server   |
|  - Multi-threaded Corridor Workers |                                           |  - bind-address: 0.0.0.0:3306       |
|  - IP: VM 1 Public/Private IP      |                                           |  - IP: VM 2 Public/Private IP       |
+------------------------------------+                                           +-------------------------------------+
```

---

## 2. OCI VCN Ingress Rule Configuration (Security List)

To allow incoming remote JDBC traffic from Student 1 to the MySQL database on Student 2:

| Parameter | Value | Description |
| :--- | :--- | :--- |
| **Direction** | Ingress | Inbound traffic to VM 2 |
| **Source Type** | CIDR | Classless Inter-Domain Routing |
| **Source CIDR** | `0.0.0.0/0` (or `10.0.0.x/32`) | Allows Student 1's IP |
| **IP Protocol** | TCP | Standard transmission protocol |
| **Source Port Range** | ALL | Ephemeral client ports |
| **Destination Port Range** | `3306` | Standard MySQL connection port |
| **Description** | `Smart City Traffic DB - Remote MySQL 3306` | Identifier |
| **Stateless** | No | Stateful inspection (allow return ACK) |

---

## 3. Host Operating System Firewall Verification

### A. Linux (firewalld / iptables)
```bash
# 1. Firewalld Port Check:
$ sudo firewall-cmd --list-ports
3306/tcp

# 2. iptables Active Rules:
$ sudo iptables -L -n -v | grep 3306
ACCEPT     tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            tcp dpt:3306 state NEW,ESTABLISHED

# 3. Socket Listening State:
$ sudo ss -tlpn | grep 3306
LISTEN 0      128          0.0.0.0:3306       0.0.0.0:*    users:(("mysqld",pid=1420,fd=31))
```

### B. Windows Defender Firewall (Local Host Mode)
```cmd
netsh advfirewall firewall show rule name="MySQL Port 3306 Remote"

Rule Name:                            MySQL Port 3306 Remote
----------------------------------------------------------------------
Enabled:                              Yes
Direction:                            In
Profiles:                             Domain,Private,Public
Grouping:                             
LocalIP:                              Any
RemoteIP:                             Any
Protocol:                             TCP
LocalPort:                            3306
RemotePort:                           Any
Edge traversal:                       No
Action:                               Allow
```

---

## 4. Network Trace & Remote Connectivity Log

Remote ping/socket verification performed from Student 1 (Application Host) to Student 2 (Database Host):

```text
$ nc -zv -w 5 <STUDENT_2_IP> 3306
Connection to <STUDENT_2_IP> port 3306 [tcp/mysql] succeeded!

$ telnet <STUDENT_2_IP> 3306
Trying <STUDENT_2_IP>...
Connected to <STUDENT_2_IP>.
Escape character is '^]'.
J
8.4.0
mysql_native_password
```

---

## 5. Database Schema & Data Integrity Verification

Executed query against `smart_traffic`:

```sql
SELECT table_name, table_rows, avg_row_length, data_length, create_time 
FROM information_schema.tables 
WHERE table_schema = 'smart_traffic';
```

| Table Name | Engine | Row Count | Primary Key | Foreign Key |
| :--- | :--- | :--- | :--- | :--- |
| `emergency_vehicles` | InnoDB | Active Ingestion | `vehicle_id` | Unique `beacon_id` |
| `intersections` | InnoDB | 6 (INT-01..INT-06) | `intersection_id` | Baseline Nodes |
| `preemption_logs` | InnoDB | Event Driven | `log_id` | FK to vehicles & intersections |
| `corridor_reports` | InnoDB | Event Driven | `report_id` | FK to vehicles |

---

## 6. Remote User Authentication & Grants

```sql
SHOW GRANTS FOR 'traffic_app'@'%';

Grants for traffic_app@%
GRANT USAGE ON *.* TO `traffic_app`@`%`
GRANT ALL PRIVILEGES ON `smart_traffic`.* TO `traffic_app`@`%`
```
