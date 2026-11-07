# Smart City Traffic Signal & Emergency Vehicle Green Corridor Router

A high-performance, multithreaded Java 21 & MySQL project that dynamically routes emergency vehicles (ambulances, fire engines, police VIP convoys) through urban traffic grids by calculating weighted priority scores, synchronizing preemption overrides, and producing green corridor transit waves.

---

## Team Division of Responsibility

| Student | Area of Focus | Status |
| :--- | :--- | :--- |
| **Student 1 (Completed Today)** | Java 21 core application, multithreading, priority scoring engine, CSV/TXT file I/O, local MySQL schema, and standalone simulation mode. | **Done & Verified** |
| **Student 2 (Scheduled for Tomorrow)** | Oracle Cloud Infrastructure (OCI) MySQL cloud database provisioning, VCN security lists, remote JDBC connectivity, and network routing. | **Configured & Ready** |

---

## System Architecture

```
                             +------------------------+
                             |   emergency_gps.csv    |
                             +-----------+------------+
                                         |
                                         v
                             +------------------------+
                             |     CsvGpsParser       | (Regex: EMG-[A-Z]{3}-\d{4})
                             +-----------+------------+
                                         |
                                         v
                         +-------------------------------+
                         |      PriorityCalculator       |
                         |  Ambulance / Fire / Police    |
                         +---------------+---------------+
                                         |
                                         v
                         +-------------------------------+
                         |     CorridorRouterService     | (ETA < 2.0 min check)
                         +---------------+---------------+
                                         |
              +--------------------------+--------------------------+
              |                          |                          |
              v                          v                          v
     [CorridorWorker-01]        [CorridorWorker-02]        [CorridorWorker-03]
   (Intersection INT-01)      (Intersection INT-02)      (Intersection INT-03)
              |                          |                          |
              +--------------------------+--------------------------+
                                         | (ExecutorService join)
                                         v
                         +-------------------------------+
                         |       PreemptionWriter        |
                         |   String.format() Log Engine  |
                         +---------------+---------------+
                                         |
                   +---------------------+---------------------+
                   |                                           |
                   v                                           v
       +-----------------------+                   +-----------------------+
       |   Output TXT Files    |                   |   TrafficDAO (JDBC)   |
       |  execution_logs.txt   |                   |  Local MySQL 8.4 LTS  |
       |  corridor_summary.txt |                   |  or OCI Remote DB     |
       +-----------------------+                   +-----------------------+
```

---

## Technology Stack

- **Language:** Java 21 LTS (`jdk-21.0.11`)
- **Build System:** Apache Maven 3.9.6
- **Database:** MySQL Community Server 8.4 LTS / Oracle Cloud (OCI) MySQL
- **Driver:** MySQL Connector/J 8.4.0 (`com.mysql:mysql-connector-j`)
- **Testing:** JUnit 5 (`5.10.2`)
- **Concurrency:** Java `ExecutorService`, `Callable`, `Future`, and worker threads
- **Data Exchange:** CSV telemetry parsing and formatted TXT report generation

---

## Project Folder Structure

```
F:\Team Java\
│
├── pom.xml                                  # Maven project configuration (Java 21, dependencies)
├── smart_traffic_schema.sql                 # MySQL 8.4 4-table schema script
├── emergency_gps.csv                        # Live telemetry and GPS dataset
├── README.md                                # Comprehensive project documentation
│
├── output/                                  # Auto-generated TXT reports and logs
│   ├── execution_logs_YYYYMMDD_HHmmss.txt
│   └── corridor_performance_EV-xxx.txt
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── smarttraffic/
│   │               ├── MainApp.java                    # Console interface & execution menu
│   │               │
│   │               ├── database/
│   │               │   ├── DBConnection.java           # JDBC manager (env var driven)
│   │               │   └── TrafficDAO.java             # PreparedStatement DAO (CRUD operations)
│   │               │
│   │               ├── models/
│   │               │   ├── VehicleCategory.java        # AMBULANCE, FIRE_ENGINE, POLICE_VIP
│   │               │   ├── PriorityTier.java           # Gold, Standard, Minor, Blocked
│   │               │   ├── SignalState.java            # RED, YELLOW, GREEN
│   │               │   ├── OverrideStatus.java         # SUCCESS, FAILED, BYPASSED
│   │               │   ├── EmergencyVehicle.java       # Phase 1 & 2 data model
│   │               │   ├── Intersection.java           # Phase 3 signal model
│   │               │   ├── PreemptionLog.java          # Event logging model
│   │               │   └── CorridorReport.java         # Summary report model
│   │               │
│   │               ├── services/
│   │               │   ├── PriorityCalculator.java     # Stage 4 mathematical scoring
│   │               │   ├── CorridorRouterService.java  # Stage 6 preemption & orchestration
│   │               │   └── CsvGpsParser.java           # CSV reader & telemetry validator
│   │               │
│   │               ├── workers/
│   │               │   └── CorridorWorker.java         # Multithreaded signal simulator
│   │               │
│   │               └── utils/
│   │                   ├── ValidationUtils.java        # Beacon regex & string cleaning
│   │                   └── PreemptionWriter.java       # String.format() report writer
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── smarttraffic/
│                   ├── ValidationUtilsTest.java
│                   ├── PriorityCalculatorTest.java
│                   ├── CorridorRouterServiceTest.java
│                   ├── CsvGpsParserTest.java
│                   ├── CorridorWorkerMultithreadingTest.java
│                   ├── PreemptionWriterTest.java
│                   └── DBConnectionAndDAOTest.java
```

---

## Detailed Class Breakdown

1. **`MainApp.java`**: Interactive console application offering menu-driven actions to inspect databases, parse telemetry, calculate priority scores, run parallel simulations, persist to MySQL, and export text files. Includes an automated `--auto` demonstration flag.
2. **`DBConnection.java`**: Resolves connection strings dynamically from `DB_URL`, `DB_USER`, and `DB_PASSWORD`. Never hardcodes credentials. Detects offline states gracefully and guides the developer.
3. **`TrafficDAO.java`**: Utilizes `PreparedStatement` across all four database tables (`emergency_vehicles`, `intersections`, `preemption_logs`, `corridor_reports`). Handles SQL transactions cleanly with try-with-resources.
4. **`CorridorWorker.java`**: Implements `Callable<PreemptionLog>` to run parallel background workers per intersection. Simulates cross-traffic red cycles, corridor green switching, and clearance delays with thread names and timestamps.
5. **`PreemptionWriter.java`**: Employs `String.format()` to construct structured tables, detailed activity traces, and exports execution logs and summaries to the `output/` folder.
6. **`PriorityCalculator.java`**: Enforces strict mathematical priority formulas for each vehicle category and clamps all inputs to $0-100$.
7. **`CorridorRouterService.java`**: Manages the thread pool (`ExecutorService`), assesses the critical condition ($\text{ETA} < 2.0\text{ minutes}$ at blocked intersections), and collects futures.
8. **`CsvGpsParser.java`**: Reads `emergency_gps.csv`, validates Beacon IDs against `EMG-[A-Z]{3}-\d{4}`, sanitizes tags with `.trim()`, and handles malformed rows without crashing.
9. **`ValidationUtils.java`**: Centralized regex validator and string parser using `.split(",")` and `.trim()`.

---

## Priority Scoring Mathematical Formulas (Stage 4)

All raw inputs are normalized to $[0, 100]$. Higher scores represent higher urgency.

### 1. Ambulance ($\text{Max Delay} = 3.0\text{ min}$)
$$\text{Score} = (\text{ETA Precision} \times 0.50) + (\text{Queue Density} \times 0.30) + (\text{Patient Criticality} \times 0.20)$$

### 2. Fire Engine ($\text{Max Delay} = 5.0\text{ min}$)
$$\text{Score} = (\text{Queue Density} \times 0.40) + (\text{Weight Clearance} \times 0.30) + (\text{ETA Precision} \times 0.30)$$

### 3. Police VIP Convoy ($\text{Max Delay} = 2.0\text{ min}$)
$$\text{Score} = (\text{Signal Synchronization} \times 0.50) + (\text{Security Clearance} \times 0.30) + (\text{Speed Maintenance} \times 0.20)$$

---

## Priority Tiers (Stage 7)

- **Gold Corridor:** $90.00 - 100.00$
- **Standard Clearance:** $75.00 - 89.99$
- **Minor Traffic Delay:** $60.00 - 74.99$
- **Route Blocked:** $< 60.00$ OR when green corridor override fails

---

## Green Corridor Override Rules (Stage 6)

- **Trigger Condition:** The override is triggered **only** if:
  $$\text{ETA} < 2.0\text{ minutes} \quad \text{AND} \quad \text{Intersection is BLOCKED}$$
- **Boundary Rule:** An $\text{ETA}$ equal to exactly $2.00$ minutes **does NOT** trigger the override. Standard clearance rules apply.
- **Simulation Flow:**
  1. Conflicting cross-traffic signals transition to `RED`.
  2. Emergency corridor direction turns `GREEN`.
  3. Consecutive intersections synchronize a green wave.
  4. Transit and clearance duration are logged.
  5. Status is recorded as `SUCCESS`, `FAILED`, or `BYPASSED`.

---

## Database Setup & Schema Execution (Stage 11)

The MySQL schema is provided in [`smart_traffic_schema.sql`](smart_traffic_schema.sql).

### Tables:
1. `emergency_vehicles`: Primary key `vehicle_id`, unique `beacon_id`, indexes on `category` and `severity_code`.
2. `intersections`: Primary key `intersection_id`, stores coordinates, signal state, and camera vehicle counts.
3. `preemption_logs`: Auto-increment `log_id`, foreign keys to `emergency_vehicles` and `intersections`, stores timestamped worker activity.
4. `corridor_reports`: Primary key `report_id`, foreign key to `emergency_vehicles`, stores transit duration and assigned tier.

### Manual SQL Execution Steps:
1. Open **MySQL Workbench** or your preferred MySQL client.
2. Connect to your local MySQL instance (port `3306`).
3. Open `F:\Team Java\smart_traffic_schema.sql`.
4. Execute the script to create the `smart_traffic` database and tables.

---

## Environment Variables Configuration

To run the application with custom credentials or against a remote database, configure the following environment variables:

### Windows PowerShell:
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER = "root"
$env:DB_PASSWORD = "YourPasswordHere"
```

### Windows Command Prompt (cmd):
```cmd
set DB_URL=jdbc:mysql://localhost:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USER=root
set DB_PASSWORD=YourPasswordHere
```

*Note: If these variables are omitted, the application uses local default credentials (`localhost:3306/smart_traffic`). If MySQL is offline, the application seamlessly activates Standalone Simulation Mode.*

---

## Build, Test & Run Instructions

### 1. Compile and Run Unit Tests:
```powershell
mvn test
```
*Executes all 34 automated unit and integration tests across scoring, multithreading, regex, CSV parsing, and database diagnostics.*

### 2. Package the Application:
```powershell
mvn clean package -DskipTests
```
*Generates the standalone JAR file at `target/SmartTrafficRouter-1.0.0.jar` and copies dependencies to `target/lib/`.*

### 3. Run the Interactive Console Application:
```powershell
java -jar target/SmartTrafficRouter-1.0.0.jar
```

### 4. Run the Automated Demo Pipeline:
```powershell
java -jar target/SmartTrafficRouter-1.0.0.jar --auto
```

---

## Sample Execution Output

```
[SYSTEM] Running Automated End-to-End Green Corridor Demonstration...

=========================================================================
          STARTING END-TO-END CORRIDOR SIMULATION PIPELINE
=========================================================================

--- [1] DATABASE CONNECTIVITY INSPECTION ---
  Configured JDBC URL : jdbc:mysql://localhost:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  Configured DB User  : root
  Testing connection...
  [-] NOTICE: Database currently offline or unreachable.
      Standalone Simulation Mode is ACTIVE. All Java core simulation,
      multithreading, priority calculation, and TXT reporting work offline!

--- [2] LOADING TELEMETRY FROM CSV: emergency_gps.csv ---
  [+] Successfully loaded and validated 6 emergency vehicles:

  VEHICLE    | BEACON ID       | CATEGORY       | SEVERITY         | SPEED(km/h) | ETA(min)
  -----------------------------------------------------------------------------------
  EV-101     | EMG-AMB-1001    | AMBULANCE      | CRITICAL_CODE_RED |       68.5 |     1.40
  EV-102     | EMG-AMB-1002    | AMBULANCE      | URGENT_YELLOW    |       52.0 |     2.00
  EV-201     | EMG-FIR-2042    | FIRE_ENGINE    | INDUSTRIAL_FIRE  |       58.0 |     1.80
  EV-202     | EMG-FIR-2043    | FIRE_ENGINE    | RESIDENTIAL_SMOKE |       42.0 |     3.50
  EV-301     | EMG-POL-3099    | POLICE_VIP     | DIPLOMAT_TRANSIT |       74.0 |     1.20
  EV-302     | EMG-POL-3100    | POLICE_VIP     | ROUTINE_ESCORT   |       48.0 |     2.50

--- [3] EMERGENCY VEHICLE PRIORITY SCORING & TIER EVALUATION ---
  VEHICLE    | CATEGORY         | FORMULA INPUTS  | SCORE    | ASSIGNED TIER         
  ----------------------------------------------------------------------------------
  EV-101     | Ambulance        | 95, 85, 90      |    91.00 | Gold Corridor         
  EV-102     | Ambulance        | 80, 45, 70      |    67.50 | Minor Traffic Delay   
  EV-201     | Fire Engine      | 75, 92, 88      |    84.00 | Standard Clearance    
  EV-202     | Fire Engine      | 40, 70, 60      |    55.00 | Route Blocked         
  EV-301     | Police VIP Convoy | 90, 95, 85      |    90.50 | Gold Corridor         
  EV-302     | Police VIP Convoy | 65, 50, 60      |    59.50 | Route Blocked         

--- [4] MULTITHREADED GREEN CORRIDOR SIMULATION ---
  >>> Dispatching EV-101 (Ambulance, ETA: 1.40 min) <<<
      [Result] Score: 91.00 | Tier: Gold Corridor | Transit: 178s | Overrides: 3/4 | Status: COMPLETED

  >>> Dispatching EV-102 (Ambulance, ETA: 2.00 min) <<<
      [Result] Score: 67.50 | Tier: Route Blocked | Transit: 480s | Overrides: 0/4 | Status: ABORTED

--- [6] EXPORTING EXECUTION LOGS & SUMMARIES (TXT) ---
  [+] Execution logs exported to : F:\Team Java\output\execution_logs_20261107_165842.txt
  [+] Summary report exported to : F:\Team Java\output\corridor_performance_EV-101.txt
```

---

## Preparation for Student 2 Tomorrow (Stage 14: OCI Cloud Integration)

Tomorrow, Student 2 will deploy the MySQL Database on **Oracle Cloud Infrastructure (OCI)**. Student 1's application is already prepared to switch seamlessly to OCI without code changes.

### Checklist for Student 2:
1. **OCI MySQL System:** Provision a MySQL Database Service (MDS) instance in your OCI compartment.
2. **Networking & VCN Security Rules:**
   - In your OCI Virtual Cloud Network (VCN), navigate to **Security Lists** (or Network Security Groups).
   - Add an **Ingress Rule** to allow TCP traffic on port `3306` (or `33060` for X-Protocol).
   - Source CIDR: Set to your development machine's public IP (e.g., `x.x.x.x/32`) or student subnet.
3. **Database Initialization:**
   - Connect via MySQL Workbench or Bastion/SSH tunnel using the OCI MySQL Private/Public IP.
   - Run [`smart_traffic_schema.sql`](smart_traffic_schema.sql) to create `smart_traffic` tables.
4. **Configure Java Application for OCI:**
   - In PowerShell, export the OCI database details:
     ```powershell
     $env:DB_URL = "jdbc:mysql://<OCI_MYSQL_ENDPOINT_OR_IP>:3306/smart_traffic?useSSL=true&serverTimezone=UTC"
     $env:DB_USER = "<OCI_DB_USERNAME>"
     $env:DB_PASSWORD = "<OCI_DB_PASSWORD>"
     ```
5. **Verify Connectivity:**
   - Run the application:
     ```powershell
     java -jar target/SmartTrafficRouter-1.0.0.jar
     ```
   - Select Option `1` from the menu: *Test Database Connectivity*.
   - Once verified, run Option `5`: *Persist Simulation Results to MySQL Database*.

---

## Common Errors & Troubleshooting

| Error | Root Cause | Solution |
| :--- | :--- | :--- |
| `CommunicationsException: Communications link failure` | MySQL service is stopped or port 3306 is not open. | Start MySQL Community Server in Windows Services (`services.msc`) or use Standalone Simulation Mode. |
| `Access denied for user 'root'@'localhost'` | MySQL password does not match local default. | Set `$env:DB_PASSWORD = "your_actual_password"` in PowerShell before launching the app. |
| `Invalid Beacon ID` warning in console | A row in `emergency_gps.csv` does not match `EMG-[A-Z]{3}-\d{4}`. | Ensure all vehicle beacon identifiers strictly follow the 3-letter, 4-digit format (e.g. `EMG-AMB-1001`). |
