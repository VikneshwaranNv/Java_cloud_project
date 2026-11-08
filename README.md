<div align="center">

# 🚦 Smart City Traffic Signal & Emergency Vehicle Green Corridor Router
### Distributed Real-Time Traffic Orchestration & Cloud Database Architecture
#### *Project Assignment Set 31 | Java 21 LTS • MySQL 8.4 LTS • Oracle Cloud Infrastructure (OCI)*

[![Java Version](https://img.shields.io/badge/Java-21%20LTS-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Maven Build](https://img.shields.io/badge/Maven-3.9.6-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Database](https://img.shields.io/badge/MySQL-8.4%20LTS-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Tests](https://img.shields.io/badge/JUnit%205-36%2F36%20Passing-brightgreen?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Cloud Host](https://img.shields.io/badge/OCI-VM%20Distributed-F80000?logo=oracle&logoColor=white)](https://www.oracle.com/cloud/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

<br/>

**A mission-critical distributed traffic management system that eliminates delays for emergency vehicles by orchestrating dynamic signal overrides, multithreaded green waves, and live telemetry persistence across distributed cloud infrastructure.**

</div>

---

## 📑 Table of Contents
- [Executive Overview](#-executive-overview)
- [Team Division of Responsibility](#-team-division-of-responsibility)
- [Distributed Architecture & 300 KM Cloud Bridge](#-distributed-architecture--300-km-cloud-bridge)
- [Core Functional Specifications](#-core-functional-specifications)
- [Mathematical Formulas](#-mathematical-formulas)
- [Attendance, Rating & Grading Rules](#-attendance-rating--grading-rules)
- [Technical Modules](#-technical-modules)
- [Submission Deliverables Matrix](#-submission-deliverables-matrix)
- [Directory Layout](#-directory-layout)
- [Installation & Quickstart](#-installation--quickstart)
- [Automated Verification & Test Proofs](#-automated-verification--test-proofs)
- [Contributors & Team Sign-Off](#-contributors--team-sign-off)

---

## 🏛️ Executive Overview

During emergency transit, every second lost at a red light directly impacts human survival. Traditional static signal timers fail to adapt to live vehicle speed, congestion bottlenecks, and multi-vehicle criticality. 

The **Smart City Traffic Signal & Emergency Vehicle Green Corridor Router** solves this by establishing an autonomous, multi-threaded preemption engine that:
1. **Ingests real-time vehicle dispatch & GPS streams** (`emergency_gps.csv`).
2. **Validates Beacon Hardware IDs** against strict regular expressions (`EMG-[A-Z]{3}-\d{4}`).
3. **Calculates category-specific priority scores** across Ambulance, Fire Engine, and Police VIP convoys.
4. **Dispatches concurrent `CorridorWorker` threads** that dynamically preempt consecutive intersection signals from RED to GREEN when vehicle $\text{ETA} < 2.0\text{ mins}$.
5. **Persists telemetry, preemption audits, and performance manifests** across distributed nodes into MySQL 8.4 tables.

---

## 👥 Team Division of Responsibility

This system was engineered as a **distributed collaborative cloud application** divided across two specialized engineering roles separated across a **300 km physical distance**:

```
+-------------------------------------------------------------------------------------------------------+
|                                    DISTRIBUTED PROJECT RESPONSIBILITIES                               |
+---------------------------------------------------+---------------------------------------------------+
|               STUDENT 1 (LAPTOP 1)                |               STUDENT 2 (LAPTOP 2)                |
|                 APPLICATION HOST                  |                   DATABASE HOST                   |
+---------------------------------------------------+---------------------------------------------------+
| • Java 21 Traffic Engine (MainApp.java)           | • MySQL 8.4 LTS Community Server / Daemon         |
| • Concurrent CorridorWorker Multithreading        | • Database Schema (`smart_traffic_schema.sql`)   |
| • Priority Scoring, ETE & CMI Math Engine         | • 6 Intersections Seed (`seed_intersections.sql`) |
| • Telemetry CSV Parsing & Beacon Regex Validation | • OCI VCN Ingress & OS Firewall Port 3306 Config  |
| • Text Audit Exporter (PreemptionWriter.java)     | • Remote TCP Tunneling & Socket Verification      |
| • TrafficDAO JDBC Remote Client Driver            | • Remote User Delegation (`traffic_app`@'%')      |
+---------------------------------------------------+---------------------------------------------------+
```

---

## 🌐 Distributed Architecture & 300 KM Cloud Bridge

```
========================================================================================================================
[ STUDENT 1: APPLICATION HOST ]                    [ WAN / INTERNET 300 KM ]               [ STUDENT 2: DATABASE HOST ]
  • Java 21 LTS Engine                                                                       • MySQL Community 8.4 LTS
  • Multi-threaded CorridorWorkers                                                           • Database: smart_traffic
  • Telemetry CSV & Beacon Parser                                                            • Port: 3306 (0.0.0.0)
  • ETE / CMI Scoring Pipeline                                                               • Remote User: traffic_app
                                              TCP Handshake over WAN
  [JDBC Remote Client]  ---------------------> (bore / ngrok / OCI) -----------------------> [InnoDB Relational Tables]
    - DB_URL: jdbc:mysql://...:PORT/smart_traffic                                              - emergency_vehicles
    - Auto-reconnect & connection pooling                                                      - intersections
    - PreparedStatements CRUD                                                                  - preemption_logs
                                                                                               - corridor_reports
========================================================================================================================
```

---

## 🎯 Core Functional Specifications

### 1. Emergency Vehicle Categories & Weighted Formulas

Each emergency vehicle category evaluates distinct field parameters normalized to a scale of $0 - 100$:

| Vehicle Category | Formula Weighting | Maximum Delay Limit |
| :--- | :--- | :--- |
| **Ambulance Emergency Corridor** | $\text{Score} = (0.50 \times \text{ETA}) + (0.30 \times \text{Queue}) + (0.20 \times \text{Criticality})$ | **$\le 3$ Minutes** |
| **Fire Engine Response** | $\text{Score} = (0.40 \times \text{Queue}) + (0.30 \times \text{Weight}) + (0.30 \times \text{ETA})$ | **$\le 5$ Minutes** |
| **Police VIP Convoy** | $\text{Score} = (0.50 \times \text{Sync}) + (0.30 \times \text{Security}) + (0.20 \times \text{Speed})$ | **$\le 2$ Minutes** |

### 2. Phased Incremental Data Entry

- **Phase 1 (Dispatch Ingestion):** Captures Emergency Vehicle ID, Vehicle Category, Severity Code, and Destination Site Code.
- **Phase 2 (Intersection Telemetry):** Captures GPS Coordinates, Live Speed (km/h), Target Intersection ID, and Live Camera Density (Count).
- **Phase 3 (Corridor Clearance Audit):** Calculates Signal Green Duration, Clearance Delay (Seconds), and Total Transit Time.

---

## 🧮 Mathematical Formulas

### 1. Emergency Transit Efficiency (ETE)
Measures transit time degradation caused by traffic friction compared against uncongested baseline travel:

$$\text{ETE} = \left(\frac{\text{Uncongested Estimated Time}}{\text{Actual Emergency Transit Time}}\right) \times 100$$

### 2. Congestion Mitigation Index (CMI)
Quantifies signal delay reduction relative to the standard urban traffic light cycle:

$$\text{CMI} = 100 - \left(\frac{\text{Intersection Delay Time}}{\text{Standard Signal Cycle}}\right) \times 100$$

---

## 🏆 Attendance, Rating & Grading Rules

### 🚦 Green Override Filter Condition
* **Rule:** If an Emergency Vehicle reaches an intersection with $\text{ETA} < 2.0\text{ minutes}$ and the intersection is blocked:
  $$\text{Action} \implies \mathbf{GREEN\_CORRIDOR\_OVERRIDE}$$
  *(All cross-traffic signals transition to RED; green wave preemption is activated on the target route).*
* **Rule:** If $\text{ETA} \ge 2.0\text{ minutes}$, standard signal cycle timing is maintained.

### 🎖️ Transit Efficiency Tiers

| Score Range | Efficiency Tier | Operational Protocol |
| :---: | :--- | :--- |
| **$90 - 100$** | **Gold Corridor** | Zero delay, seamless synchronized green transit wave. |
| **$75 - 89$** | **Standard Clearance** | Normal signal preemption cycle with minor queue discharge. |
| **$60 - 74$** | **Minor Traffic Delay** | Congestion slowdown review triggered for intersection engineers. |
| **$< 60$** | **Route Blocked** | Override failed or congestion critical; automated reroute alert issued. |

---

## ⚙️ Technical Modules

| Technical Requirement | Implementation Detail | Source File |
| :--- | :--- | :--- |
| **String Processing** | Validates Beacon Codes via regex `^EMG-[A-Z]{3}-\d{4}$`; parses camera feeds with `.split(",")`; strips location tags via `.trim()`; formats audit logs with `String.format()`. | [`ValidationUtils.java`](src/main/java/com/smarttraffic/utils/ValidationUtils.java) |
| **File Handling (I/O)** | Ingests GPS streams from `emergency_gps.csv`; exports formatted preemption execution logs and corridor performance summaries to `.txt`. | [`CsvGpsParser.java`](src/main/java/com/smarttraffic/services/CsvGpsParser.java), [`PreemptionWriter.java`](src/main/java/com/smarttraffic/utils/PreemptionWriter.java) |
| **Multithreading** | Uses `ExecutorService` and `Callable<PreemptionLog>` worker threads to concurrently evaluate and toggle signals across consecutive city nodes. | [`CorridorWorker.java`](src/main/java/com/smarttraffic/workers/CorridorWorker.java), [`CorridorRouterService.java`](src/main/java/com/smarttraffic/services/CorridorRouterService.java) |
| **Database Persistence** | Connects over JDBC with environment-driven credentials; executes batch PreparedStatement transactions against 4 relational tables. | [`DBConnection.java`](src/main/java/com/smarttraffic/database/DBConnection.java), [`TrafficDAO.java`](src/main/java/com/smarttraffic/database/TrafficDAO.java) |

---

## 📋 Submission Deliverables Matrix

| Deliverable Name | File Link / Artifact | Responsibility | Verified Status |
| :--- | :--- | :---: | :---: |
| **Database Schema DDL** | [`smart_traffic_schema.sql`](smart_traffic_schema.sql) | Student 2 | ✅ Verified in MySQL 8.4 |
| **Intersection Baseline Seed** | [`seed_intersections.sql`](seed_intersections.sql) | Student 2 | ✅ 6 Intersections Active |
| **Application Host Engine** | [`MainApp.java`](src/main/java/com/smarttraffic/MainApp.java) | Student 1 | ✅ Interactive Menu 1–9 |
| **JDBC Database Manager** | [`DBConnection.java`](src/main/java/com/smarttraffic/database/DBConnection.java) | Student 1 | ✅ Zero Hardcoded Creds |
| **Relational Data Access Object** | [`TrafficDAO.java`](src/main/java/com/smarttraffic/database/TrafficDAO.java) | Student 1 | ✅ Full CRUD Operations |
| **Concurrent Preemption Worker** | [`CorridorWorker.java`](src/main/java/com/smarttraffic/workers/CorridorWorker.java) | Student 1 | ✅ Thread Pool Verified |
| **Audit Exporter & Formatter** | [`PreemptionWriter.java`](src/main/java/com/smarttraffic/utils/PreemptionWriter.java) | Student 1 | ✅ Formatted TXT Reports |
| **Architecture Verification Doc** | [`ARCHITECTURE_VERIFICATION_DOCUMENT.md`](ARCHITECTURE_VERIFICATION_DOCUMENT.md) | Student 2 | ✅ Port 3306 & Firewall |
| **Student 2 Final Dossier** | [`STUDENT2_SUBMISSION_REPORT.md`](STUDENT2_SUBMISSION_REPORT.md) | Student 2 | ✅ College Submission Ready |
| **Presentation & Viva Guide** | [`PRESENTATION_GUIDE.md`](PRESENTATION_GUIDE.md) | Both | ✅ Viva Q&A Guide Ready |

---

## 📁 Directory Layout

```
F:\Team Java\
├── pom.xml                                      # Maven Java 21 build configuration
├── smart_traffic_schema.sql                     # Relational DDL (4 tables, constraints, indexes)
├── seed_intersections.sql                       # Intersections INT-01..INT-06 baseline seed
├── emergency_gps.csv                            # Live vehicle GPS and camera telemetry
├── verify_database.sql                          # Verification query script for database audits
├── ARCHITECTURE_VERIFICATION_DOCUMENT.md        # OCI & OS Firewall verification evidence
├── STUDENT2_SUBMISSION_REPORT.md                # Official college completion report for Student 2
├── PRESENTATION_GUIDE.md                        # Complete Viva Voce and project defense guide
├── setup_windows_database_host.bat              # 1-click Windows MySQL & Firewall configurator
├── setup_oci_database_host.sh                   # 1-click Linux OCI VM 2 database provisioner
├── test_remote_db.bat                           # Interactive database query test script
├── connect_remote_student2.bat                  # Student 1 launcher connecting across 300 km
├── output/                                      # Auto-generated TXT reports and audit trails
│   ├── execution_logs_20261108_xxxxxx.txt
│   └── corridor_performance_EV-xxx.txt
└── src/
    ├── main/java/com/smarttraffic/
    │   ├── MainApp.java                         # Main 9-module interactive CLI & --auto runner
    │   ├── database/
    │   │   ├── DBConnection.java                # Environment-driven JDBC connection manager
    │   │   └── TrafficDAO.java                  # Relational DAO with single-connection batching
    │   ├── models/
    │   │   ├── EmergencyVehicle.java            # Vehicle telemetry model
    │   │   ├── Intersection.java                # Intersection state model
    │   │   ├── PreemptionLog.java               # Millisecond override log model
    │   │   ├── CorridorReport.java              # Corridor performance summary model
    │   │   ├── VehicleCategory.java             # AMBULANCE, FIRE_ENGINE, POLICE_VIP
    │   │   ├── SignalState.java                 # RED, YELLOW, GREEN
    │   │   ├── OverrideStatus.java              # SUCCESS, FAILED, BYPASSED
    │   │   └── PriorityTier.java                # GOLD, STANDARD, MINOR, BLOCKED
    │   ├── services/
    │   │   ├── PriorityCalculator.java          # Category weighting, ETE & CMI formulas
    │   │   ├── CorridorRouterService.java       # Preemption evaluator & thread coordinator
    │   │   └── CsvGpsParser.java                # Telemetry file reader
    │   ├── utils/
    │   │   ├── ValidationUtils.java             # Regex validator & string processing utilities
    │   │   └── PreemptionWriter.java            # Text exporter & String.format() generator
    │   └── workers/
    │       └── CorridorWorker.java              # Callable worker thread for signal switching
    └── test/java/com/smarttraffic/
        ├── PriorityCalculatorTest.java          # Unit tests for scoring & formulas
        ├── CorridorRouterServiceTest.java       # Unit tests for override conditions
        ├── CorridorWorkerMultithreadingTest.java# Concurrency and thread safety tests
        ├── ValidationUtilsTest.java             # Regex and string parsing tests
        ├── CsvGpsParserTest.java                # CSV ingestion tests
        ├── PreemptionWriterTest.java            # TXT audit export tests
        └── DBConnectionAndDAOTest.java          # JDBC and DAO mock/fallback tests
```

---

## 🚀 Installation & Quickstart

### Prerequisites
- **Java Development Kit (JDK):** Version 21 LTS or higher (`java -version`)
- **Build Tool:** Apache Maven 3.8+ (`mvn -version`)
- **Database:** MySQL Community Server 8.4 LTS

### 1. Build the Application (Student 1)
```bash
git clone https://github.com/VikneshwaranNv/Java_cloud_project.git
cd Java_cloud_project
mvn clean package
```
*Compiled self-contained executable JAR produced at:* `target/SmartTrafficRouter-1.0.0.jar`

### 2. Set Up Database Host (Student 2)
On Windows:
```cmd
setup_windows_database_host.bat
```
On Linux / OCI VM 2:
```bash
chmod +x setup_oci_database_host.sh
./setup_oci_database_host.sh
```

### 3. Run Distributed Simulation Across 300 KM
Set environment variables pointing to Student 2's host:

```powershell
$env:DB_URL="jdbc:mysql://<STUDENT_2_HOST>:<PORT>/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="traffic_app"
$env:DB_PASSWORD="SmartCity2026!"

java -jar target/SmartTrafficRouter-1.0.0.jar --auto
```

*(Or simply execute `./connect_remote_student2.bat`).*

---

## 🧪 Automated Verification & Test Proofs

### 1. Automated Test Suite (36 / 36 Passing)
Execute all unit tests using Maven:
```bash
mvn test
```
```text
[INFO] Running com.smarttraffic.PriorityCalculatorTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.052 s
[INFO] Running com.smarttraffic.CorridorRouterServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.038 s
[INFO] Running com.smarttraffic.CorridorWorkerMultithreadingTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.125 s
[INFO] Running com.smarttraffic.ValidationUtilsTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s
[INFO] Running com.smarttraffic.CsvGpsParserTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.033 s
[INFO] Running com.smarttraffic.PreemptionWriterTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s
[INFO] Running com.smarttraffic.DBConnectionAndDAOTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS - ALL 36 TESTS PASSED (0 FAILURES)
[INFO] ------------------------------------------------------------------------
```

### 2. Live Database Ingestion Verification (MySQL 8.4)
```sql
USE smart_traffic;
SELECT 
    (SELECT COUNT(*) FROM emergency_vehicles) AS vehicles_ingested,
    (SELECT COUNT(*) FROM intersections)      AS intersections_count,
    (SELECT COUNT(*) FROM preemption_logs)    AS preemption_logs_count,
    (SELECT COUNT(*) FROM corridor_reports)   AS corridor_reports_count;
```
**Live Result from Student 2's Database Host:**
```text
+-------------------+---------------------+-----------------------+------------------------+
| vehicles_ingested | intersections_count | preemption_logs_count | corridor_reports_count |
+-------------------+---------------------+-----------------------+------------------------+
|                 6 |                   6 |                    75 |                     12 |
+-------------------+---------------------+-----------------------+------------------------+
```

---

## 👨‍💻 Contributors & Team Sign-Off

* **Student 1 (Application Host):** Vikneshwaran N. V. — Application Architecture, Java 21 Concurrency, Scoring Algorithms, String/Regex Parsing, and File I/O.
* **Student 2 (Database Host):** Database Infrastructure, MySQL 8.4 Provisioning, OCI VCN Ingress & Firewall Configuration, and Remote Network Persistence.

---

<div align="center">
  <b>Smart City Traffic Signal & Emergency Vehicle Green Corridor Router</b><br/>
  <i>Engineered for Reliability • Optimized for Transit Speed • Grounded in Resilient Cloud Architecture</i>
</div>
