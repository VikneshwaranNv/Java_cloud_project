# OCI VM 1 Deployment & Execution Guide
## Running the Smart City Traffic Signal & Emergency Vehicle Green Corridor Router

This guide explains how to deploy and execute **Student 1's Java 21 Traffic Engine** on **Oracle Cloud Infrastructure (OCI) Virtual Machine 1 (VM 1)** and connect it to **Student 2's MySQL Database** (hosted on OCI VM 2 or OCI MySQL Database Service).

---

## 1. Cloud Architecture Overview

```
 [ Local Dev PC / VS Code ]
           |
       (SSH / SCP)
           v
 +-------------------------------------------------------------------------+
 |                     ORACLE CLOUD INFRASTRUCTURE (OCI)                   |
 |                                                                         |
 |   +----------------------------+       +----------------------------+   |
 |   |        OCI VM 1            |       |     OCI VM 2 / MDS         |   |
 |   | (Smart City Traffic Engine)|       |   (MySQL Community 8.4)    |   |
 |   |                            |       |                            |   |
 |   |  * Java 21 OpenJDK         |       |  * Database: smart_traffic |   |
 |   |  * Multithreaded Router    | ====> |  * Port: 3306 (TCP)        |   |
 |   |  * ExecutorService Workers | (JDBC)|  * Security List Ingress   |   |
 |   |  * String & Regex Engine   |       |  * 4 InnoDB Tables         |   |
 |   +----------------------------+       +----------------------------+   |
 |                                                                         |
 +-------------------------------------------------------------------------+
```

---

## 2. Prerequisites for OCI VM 1

1. **OCI Compute Instance (VM 1):**
   - Shape: `VM.Standard.E2.1.Micro` (Always Free) or `VM.Standard.A1.Flex`
   - OS: **Oracle Linux 8 / 9** or **Ubuntu 22.04 / 24.04**
   - Public IP Address assigned.
   - SSH Key Pair downloaded (`id_rsa` or `.key` file).

2. **VCN Security List Configuration (Student 2's Responsibility):**
   - Ensure the OCI Virtual Cloud Network (VCN) allows ingress traffic on port **`3306`** from VM 1's Private IP (e.g., `10.0.0.0/16`) to the Database VM.

---

## 3. Step 1: Package the OCI VM 1 Deployment Bundle

We have created an automated PowerShell script to package all necessary files into a single deployable ZIP (`oci_vm1_bundle.zip`):

In your local PowerShell terminal:
```powershell
# Create deployment package
Compress-Archive -Path target\SmartTrafficRouter-1.0.0.jar, target\lib, emergency_gps.csv, smart_traffic_schema.sql, deploy_oci_vm1.sh, smart-traffic.service -DestinationPath oci_vm1_bundle.zip -Force
```

---

## 4. Step 2: Upload Bundle to OCI VM 1 via SCP

From your local machine (PowerShell or Command Prompt), upload the bundle using your OCI SSH private key:

```powershell
# For Oracle Linux (default user: opc)
scp -i "C:\path\to\your-oci-key.key" oci_vm1_bundle.zip opc@<OCI_VM1_PUBLIC_IP>:~/

# For Ubuntu (default user: ubuntu)
# scp -i "C:\path\to\your-oci-key.key" oci_vm1_bundle.zip ubuntu@<OCI_VM1_PUBLIC_IP>:~/
```

---

## 5. Step 3: Connect to OCI VM 1 via SSH

```powershell
ssh -i "C:\path\to\your-oci-key.key" opc@<OCI_VM1_PUBLIC_IP>
```

---

## 6. Step 4: Extract and Install Java 21 on OCI VM 1

Once connected to your OCI VM 1 terminal:

```bash
# 1. Unzip the deployment bundle
unzip oci_vm1_bundle.zip -d smart-traffic
cd smart-traffic

# 2. Make the launcher script executable
chmod +x deploy_oci_vm1.sh

# 3. Install Java 21 OpenJDK
# On Oracle Linux / RHEL:
sudo dnf install -y java-21-openjdk java-21-openjdk-devel

# On Ubuntu:
# sudo apt-get update && sudo apt-get install -y openjdk-21-jdk

# Verify Java 21 installation:
java -version
```

---

## 7. Step 5: Run the Smart City Traffic Engine on OCI VM 1

### Mode A: Automated Pipeline Run (Recommended for Presentation)
Export the database connection details pointing to your database instance (e.g. VM 2 Private IP) and run:

```bash
export DB_URL="jdbc:mysql://<OCI_DB_PRIVATE_IP>:3306/smart_traffic?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="root"
export DB_PASSWORD="<OCI_DB_PASSWORD>"

java -jar SmartTrafficRouter-1.0.0.jar --auto
```

### Mode B: Interactive Terminal Run
```bash
java -jar SmartTrafficRouter-1.0.0.jar
```
*(Select Menu Options 1–8 to demonstrate individual features).*

### Mode C: Using the All-in-One Deployment Script
```bash
./deploy_oci_vm1.sh --auto
```

---

## 8. Step 6 (Optional): Run as an OCI Cloud Background Service (Systemd)

To demonstrate that the Traffic Engine runs as an enterprise 24/7 smart city service:

```bash
# 1. Move files to /opt/smart-traffic
sudo mkdir -p /opt/smart-traffic
sudo cp -r * /opt/smart-traffic/
sudo chown -R opc:opc /opt/smart-traffic

# 2. Copy systemd service file
sudo cp smart-traffic.service /etc/systemd/system/

# 3. Update the DB credentials in the service file
sudo nano /etc/systemd/system/smart-traffic.service

# 4. Enable and start the cloud service
sudo systemctl daemon-reload
sudo systemctl enable --now smart-traffic.service

# 5. Check real-time cloud service status
sudo systemctl status smart-traffic.service

# 6. Stream live preemption logs
tail -f /opt/smart-traffic/output/engine.log
```

---

## 9. Verification Checklist on OCI VM 1

- [ ] `java -version` returns Java 21.
- [ ] Port 3306 can be reached from VM 1 (`nc -zv <OCI_DB_IP> 3306`).
- [ ] Preemption signals show parallel worker threads: `[CorridorWorker-XX]`.
- [ ] Emergency vehicle records and corridor reports persist into OCI MySQL database.
- [ ] Summary reports generated in `/opt/smart-traffic/output/` or `~/smart-traffic/output/`.
