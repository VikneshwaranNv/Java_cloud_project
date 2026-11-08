package com.smarttraffic;

import com.smarttraffic.database.DBConnection;
import com.smarttraffic.database.TrafficDAO;
import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.SignalState;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.services.CorridorRouterService;
import com.smarttraffic.services.CsvGpsParser;
import com.smarttraffic.services.PriorityCalculator;
import com.smarttraffic.utils.PreemptionWriter;
import com.smarttraffic.utils.ValidationUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main application console interface.
 * Implements all Core Functional, Evaluation, Mathematical, and Technical Specifications:
 * - Section 2: Core Functional & Evaluation Specifications (Ambulance, Fire, Police VIP, Phased Data Entry)
 * - Section 3: Attendance, Rating & Grading Rules (Green Override Filter, Transit Efficiency Tiers)
 * - Section 4: Mathematical Formulas (Emergency Transit Efficiency - ETE & Congestion Mitigation Index - CMI)
 * - Section 5: Technical Modules (String Processing, File I/O, Multithreading)
 * - Section 6: Submission Deliverables (All modules integrated into Main Menu)
 */
public class MainApp {

    private final CsvGpsParser csvParser;
    private final PriorityCalculator priorityCalculator;
    private final CorridorRouterService routerService;
    private final PreemptionWriter preemptionWriter;
    private final TrafficDAO trafficDAO;

    private List<EmergencyVehicle> loadedVehicles;
    private List<Intersection> sampleCorridor;
    private final List<PreemptionLog> allRecordedLogs;
    private final List<CorridorReport> allRecordedReports;

    public MainApp() {
        this.csvParser = new CsvGpsParser();
        this.priorityCalculator = new PriorityCalculator();
        this.routerService = new CorridorRouterService(priorityCalculator);
        this.preemptionWriter = new PreemptionWriter("output");
        this.trafficDAO = new TrafficDAO();

        this.loadedVehicles = new ArrayList<>();
        this.allRecordedLogs = new ArrayList<>();
        this.allRecordedReports = new ArrayList<>();
        initializeDefaultIntersections();
    }

    private void initializeDefaultIntersections() {
        sampleCorridor = new ArrayList<>();
        sampleCorridor.add(new Intersection("INT-01", "Central Boulevard & 1st Ave", 12.9716, 77.5946, SignalState.RED, 84, true, 45, 5));
        sampleCorridor.add(new Intersection("INT-02", "Central Boulevard & 4th Cross", 12.9735, 77.5982, SignalState.RED, 62, true, 50, 6));
        sampleCorridor.add(new Intersection("INT-03", "Metro Junction Expressway", 12.9760, 77.6015, SignalState.RED, 78, true, 60, 5));
        sampleCorridor.add(new Intersection("INT-04", "Tech Corridor North Gate", 12.9810, 77.6090, SignalState.YELLOW, 35, false, 40, 4));
    }

    public static void main(String[] args) {
        MainApp app = new MainApp();

        if (args.length > 0 && "--auto".equalsIgnoreCase(args[0])) {
            System.out.println("\n[SYSTEM] Running Automated End-to-End Green Corridor Demonstration...\n");
            app.runEndToEndPipeline();
            return;
        }

        app.runInteractiveMenu();
    }

    public void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        printBanner();

        while (!exit) {
            printMenu();
            System.out.print("Select an evaluation module [1-9]: ");
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "9";

            switch (input) {
                case "1":
                    runPhasedDataEntryModule(scanner);
                    break;
                case "2":
                    runStringProcessingModule(scanner);
                    break;
                case "3":
                    runPriorityScoringModule();
                    break;
                case "4":
                    runMathematicalFormulasModule(scanner);
                    break;
                case "5":
                    runMultithreadingModule();
                    break;
                case "6":
                    runFileIoModule();
                    break;
                case "7":
                    runDatabaseModule();
                    break;
                case "8":
                    runEndToEndPipeline();
                    break;
                case "9":
                    System.out.println("\nExiting Smart City Traffic Router. Thank you!");
                    exit = true;
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please choose an option from 1 to 9.");
            }

            if (!exit) {
                System.out.println("\nPress Enter to return to main menu...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
        }
    }

    private void printBanner() {
        System.out.println("=========================================================================================");
        System.out.println("     SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER");
        System.out.println("     Complete Core Functional, Technical & Mathematical Evaluation Engine");
        System.out.println("=========================================================================================");
    }

    private void printMenu() {
        System.out.println("\n----------------------------- MAIN APPLICATION MODULES -----------------------------");
        System.out.println("1. Phased Incremental Data Entry (Phase 1: Dispatch | Phase 2: Telemetry | Phase 3: Audit)");
        System.out.println("2. Technical String Processing Module (Beacon Regex, Camera Split, Location Trim)");
        System.out.println("3. Priority Scoring & Tier Engine (Ambulance 50/30/20 | Fire 40/30/30 | Police 50/30/20)");
        System.out.println("4. Mathematical Formulas Module (ETE: Emergency Transit Efficiency & CMI: Congestion Index)");
        System.out.println("5. Multithreaded Dynamic Signal Preemption (Corridor Workers & Green Wave Waveform)");
        System.out.println("6. File Handling (I/O) Module (Stream emergency_gps.csv & Export TXT Reports)");
        System.out.println("7. Database Management & Persistence Audit (MySQL InnoDB CRUD & Connection Probe)");
        System.out.println("8. Run Complete End-to-End Evaluation Pipeline (--auto)");
        System.out.println("9. Exit Application");
        System.out.println("------------------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // MODULE 1: Phased Incremental Data Entry
    // -------------------------------------------------------------------------
    public void runPhasedDataEntryModule(Scanner scanner) {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 1: PHASED INCREMENTAL DATA ENTRY & TELEMETRY INGESTION");
        System.out.println("=========================================================================");
        System.out.println("Phase 1 (Dispatch Ingestion)       : Vehicle ID, Category, Severity Code, Destination Code");
        System.out.println("Phase 2 (Intersection Telemetry)  : GPS (Lat/Lon), Speed (km/h), Intersection ID, Camera Density Count");
        System.out.println("Phase 3 (Corridor Clearance Audit) : Signal Green Duration (s), Clearance Delay (s), Transit Time (s)\n");

        if (loadedVehicles.isEmpty()) {
            loadGpsDataFromCsv("emergency_gps.csv");
        }

        System.out.println("Current Phased Ingestion Snapshot (First 3 Active Vehicles):");
        int count = 0;
        for (EmergencyVehicle v : loadedVehicles) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.printf("[PHASE 1 - DISPATCH INGESTION]\n");
            System.out.printf("  * Vehicle ID       : %s\n", v.getVehicleId());
            System.out.printf("  * Beacon ID        : %s\n", v.getBeaconId());
            System.out.printf("  * Category Class   : %s (Max Delay: %.1f min)\n", v.getCategory().getDisplayName(), v.getCategory().getMaxDelayMinutes());
            System.out.printf("  * Severity Code    : %s\n", v.getSeverityCode());
            System.out.printf("  * Destination Site : %s\n", v.getDestinationCode());

            System.out.printf("[PHASE 2 - INTERSECTION TELEMETRY]\n");
            System.out.printf("  * GPS Coordinates  : %.6f, %.6f\n", v.getLatitude(), v.getLongitude());
            System.out.printf("  * Current Speed    : %.1f km/h\n", v.getSpeedKmh());
            System.out.printf("  * Intersection Ref : %s\n", v.getCurrentIntersectionId());
            System.out.printf("  * Camera Density   : %d vehicles\n", v.getCameraDensity());
            System.out.printf("  * Estimated ETA    : %.2f minutes\n", v.getEtaMinutes());

            System.out.printf("[PHASE 3 - CORRIDOR CLEARANCE AUDIT]\n");
            int greenDuration = 45;
            int clearanceDelay = 5;
            int totalTransit = (int) Math.round((500.0 / (Math.max(20.0, v.getSpeedKmh()) * (1000.0 / 3600.0))) + clearanceDelay);
            System.out.printf("  * Signal Green Time: %d seconds\n", greenDuration);
            System.out.printf("  * Clearance Delay  : %d seconds\n", clearanceDelay);
            System.out.printf("  * Total Transit Time: %d seconds\n", totalTransit);

            count++;
            if (count >= 3) break;
        }

        System.out.println("-------------------------------------------------------------------------");
        System.out.print("\nWould you like to manually input a new emergency vehicle record? (y/N): ");
        String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "n";
        if ("y".equalsIgnoreCase(choice)) {
            try {
                System.out.println("\n[ENTER PHASE 1 DATA]");
                System.out.print("  Enter Vehicle ID (e.g. EV-999): ");
                String vid = scanner.nextLine().trim();
                System.out.print("  Enter Beacon Code (Pattern: EMG-[A-Z]{3}-\\d{4}, e.g. EMG-AMB-9999): ");
                String beacon = scanner.nextLine().trim();
                if (!ValidationUtils.isValidBeaconId(beacon)) {
                    System.out.println("  [!] Invalid Beacon format. Must match EMG-[A-Z]{3}-\\d{4}. Aborted.");
                    return;
                }
                System.out.print("  Select Category [1: AMBULANCE, 2: FIRE_ENGINE, 3: POLICE_VIP]: ");
                String catChoice = scanner.nextLine().trim();
                VehicleCategory cat = switch (catChoice) {
                    case "2" -> VehicleCategory.FIRE_ENGINE;
                    case "3" -> VehicleCategory.POLICE_VIP;
                    default -> VehicleCategory.AMBULANCE;
                };
                System.out.print("  Enter Severity Code (e.g. CRITICAL_ALPHA): ");
                String sev = scanner.nextLine().trim();
                System.out.print("  Enter Destination Hospital/Site: ");
                String dest = scanner.nextLine().trim();

                System.out.println("\n[ENTER PHASE 2 DATA]");
                System.out.print("  Enter Current Speed (km/h): ");
                double spd = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("  Enter Current Intersection ID (e.g. INT-01): ");
                String intId = scanner.nextLine().trim();
                System.out.print("  Enter Camera Vehicle Density Count: ");
                int dens = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("  Enter Estimated Arrival ETA in Minutes: ");
                double eta = Double.parseDouble(scanner.nextLine().trim());

                System.out.println("\n[ENTER PARAMETERS FOR SCORING (0-100)]");
                System.out.print("  Enter Param 1: ");
                double p1 = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("  Enter Param 2: ");
                double p2 = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("  Enter Param 3: ");
                double p3 = Double.parseDouble(scanner.nextLine().trim());

                EmergencyVehicle customVehicle = new EmergencyVehicle(
                    vid, beacon, cat, sev, dest, 12.9716, 77.5946, spd, intId, dens, eta, p1, p2, p3
                );
                loadedVehicles.add(0, customVehicle);
                System.out.printf("\n[+] SUCCESS: New vehicle %s (%s) added to active routing pipeline!\n", vid, cat.getDisplayName());
            } catch (Exception e) {
                System.err.println("[-] Error parsing manual input: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // MODULE 2: Technical String Processing Module
    // -------------------------------------------------------------------------
    public void runStringProcessingModule(Scanner scanner) {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 2: TECHNICAL STRING PROCESSING ENGINE");
        System.out.println("=========================================================================");
        System.out.println("1. Vehicle Beacon Code Validation Regex : EMG-[A-Z]{3}-\\d{4}");
        System.out.println("2. Camera Data Parsing Tokenizer        : split(\",\")");
        System.out.println("3. Location String Sanitizer             : trim()");
        System.out.println("4. Preemption Log Formatter             : String.format()\n");

        System.out.println("--- [TEST 1: BEACON CODE REGEX VALIDATION] ---");
        String[] testBeacons = {"EMG-AMB-1001", "EMG-FIR-2042", "EMG-POL-3099", "emg-amb-1001", "AMB-1001", "EMG-AMB-12"};
        for (String b : testBeacons) {
            boolean valid = ValidationUtils.isValidBeaconId(b);
            System.out.printf("  * '%-15s' -> %s\n", b, valid ? "[VALID BEACON CODE]" : "[REJECTED: INVALID PATTERN]");
        }

        System.out.println("\n--- [TEST 2: CAMERA TELEMETRY PARSING via split(\",\") and trim()] ---");
        String rawCameraSample = "  INT-01  ,   84 vehicles   ,  BLOCKED_CROSS_TRAFFIC   ";
        System.out.printf("  * Raw Camera String: \"%s\"\n", rawCameraSample);
        String[] tokens = ValidationUtils.parseCameraData(rawCameraSample);
        for (int i = 0; i < tokens.length; i++) {
            System.out.printf("    Token [%d]: \"%s\"\n", i, tokens[i]);
        }

        System.out.println("\n--- [TEST 3: FORMATTED SIGNAL LOGS via String.format()] ---");
        String formattedSample = String.format(
            "[%s] Signal Preemption Event: Intersection=%s | Transition=%s -> %s | Transit=%ds",
            "12:00:05.120", "INT-01", SignalState.RED, SignalState.GREEN, 26
        );
        System.out.printf("  * %s\n", formattedSample);

        System.out.print("\nTest your own Beacon ID code: ");
        String customBeacon = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (!customBeacon.isEmpty()) {
            boolean result = ValidationUtils.isValidBeaconId(customBeacon);
            System.out.printf("  Result for '%s': %s\n", customBeacon, result ? "[PASSED]" : "[FAILED: Does not match EMG-[A-Z]{3}-\\d{4}]");
        }
    }

    // -------------------------------------------------------------------------
    // MODULE 3: Priority Scoring & Tier Engine
    // -------------------------------------------------------------------------
    public void runPriorityScoringModule() {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 3: EMERGENCY VEHICLE PRIORITY SCORING & TIER EVALUATION");
        System.out.println("=========================================================================");
        System.out.println("Scoring Formulas (Inputs Normalized 0-100):");
        System.out.println("  * AMBULANCE    : (ETA Precision * 0.50) + (Queue Density * 0.30) + (Patient Criticality * 0.20) | Max Delay: 3 min");
        System.out.println("  * FIRE ENGINE  : (Queue Density * 0.40) + (Weight Clearance * 0.30) + (ETA Precision * 0.30)  | Max Delay: 5 min");
        System.out.println("  * POLICE VIP   : (Signal Sync * 0.50) + (Security Clearance * 0.30) + (Speed Maintenance * 0.20) | Max Delay: 2 min\n");

        System.out.println("Transit Efficiency Tiers:");
        System.out.println("  * Gold Corridor        : 90 - 100 Score (Zero Delay / Seamless Transit)");
        System.out.println("  * Standard Clearance   : 75 - 89 Score  (Normal Signal Preemption)");
        System.out.println("  * Minor Traffic Delay  : 60 - 74 Score  (Congestion Slowdown Review)");
        System.out.println("  * Route Blocked        : < 60 Score or Failed Override (Reroute Alert Issued)\n");

        if (loadedVehicles.isEmpty()) {
            loadGpsDataFromCsv("emergency_gps.csv");
        }

        System.out.printf("  %-10s | %-16s | %-15s | %-8s | %-22s\n",
            "VEHICLE", "CATEGORY", "FORMULA INPUTS", "SCORE", "ASSIGNED TIER");
        System.out.println("  ----------------------------------------------------------------------------------");

        for (EmergencyVehicle v : loadedVehicles) {
            double score = priorityCalculator.calculateScore(v);
            priorityCalculator.assignTier(v, true);

            String inputs = String.format("%.0f, %.0f, %.0f", v.getParam1(), v.getParam2(), v.getParam3());
            System.out.printf("  %-10s | %-16s | %-15s | %8.2f | %-22s\n",
                v.getVehicleId(), v.getCategory().getDisplayName(), inputs, score, v.getPriorityTier().getLabel());
        }
    }

    // -------------------------------------------------------------------------
    // MODULE 4: Mathematical Formulas Module (ETE & CMI)
    // -------------------------------------------------------------------------
    public void runMathematicalFormulasModule(Scanner scanner) {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 4: MATHEMATICAL PERFORMANCE FORMULAS (ETE & CMI)");
        System.out.println("=========================================================================");
        System.out.println("Formula 1: Emergency Transit Efficiency (ETE)");
        System.out.println("  ETE = (UncongestedEstimatedTime / ActualEmergencyTransitTime) * 100\n");
        System.out.println("Formula 2: Congestion Mitigation Index (CMI)");
        System.out.println("  CMI = 100 - (IntersectionDelayTime / StandardSignalCycle) * 100\n");

        if (allRecordedReports.isEmpty()) {
            System.out.println("  [*] Running live simulation to generate corridor transit metrics...");
            runCorridorSimulation();
        }

        System.out.println("\nCalculated Mathematical Metrics for Evaluated Corridors:");
        System.out.printf("  %-10s | %-16s | %-12s | %-14s | %-14s\n",
            "VEHICLE", "TIER", "TRANSIT(s)", "ETE SCORE (%)", "CMI INDEX (%)");
        System.out.println("  -----------------------------------------------------------------------");

        for (CorridorReport rep : allRecordedReports) {
            System.out.printf("  %-10s | %-16s | %10ds | %13.2f%% | %13.2f%%\n",
                rep.getVehicleId(), rep.getPriorityTier().getLabel(), rep.getTotalTransitSeconds(),
                rep.getEteScore(), rep.getCmiScore()
            );
        }

        System.out.print("\nWould you like to test custom values in the ETE and CMI formulas? (y/N): ");
        String testChoice = scanner.hasNextLine() ? scanner.nextLine().trim() : "n";
        if ("y".equalsIgnoreCase(testChoice)) {
            try {
                System.out.print("  Enter Uncongested Estimated Time (seconds): ");
                double uncongested = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("  Enter Actual Emergency Transit Time (seconds): ");
                double actual = Double.parseDouble(scanner.nextLine().trim());
                double eteCalc = priorityCalculator.calculateEmergencyTransitEfficiency(uncongested, actual);
                System.out.printf("  [+] Calculated ETE = %.2f%%\n", eteCalc);

                System.out.print("\n  Enter Intersection Delay Time (seconds): ");
                double delay = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("  Enter Standard Signal Cycle Duration (seconds, e.g. 90 or 120): ");
                double cycle = Double.parseDouble(scanner.nextLine().trim());
                double cmiCalc = priorityCalculator.calculateCongestionMitigationIndex(delay, cycle);
                System.out.printf("  [+] Calculated CMI = %.2f%%\n", cmiCalc);
            } catch (Exception e) {
                System.err.println("[-] Error calculating custom formula: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // MODULE 5: Multithreaded Dynamic Signal Preemption
    // -------------------------------------------------------------------------
    public void runMultithreadingModule() {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 5: MULTITHREADED DYNAMIC SIGNAL PREEMPTION ENGINE");
        System.out.println("=========================================================================");
        System.out.println("Green Override Filter:");
        System.out.println("  * If ETA < 2.0 mins at blocked intersection: FORCES GREEN_CORRIDOR_OVERRIDE");
        System.out.println("  * If ETA == 2.0 mins: Does NOT trigger override (Standard cycle maintained)");
        System.out.println("  * Multithreading: Parallel CorridorWorker threads dynamically switch signals\n");

        runCorridorSimulation();
    }

    // -------------------------------------------------------------------------
    // MODULE 6: File Handling (I/O) Module
    // -------------------------------------------------------------------------
    public void runFileIoModule() {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 6: FILE HANDLING (I/O) & AUDIT EXPORT ENGINE");
        System.out.println("=========================================================================");
        System.out.println("1. Ingestion: Reads real-time vehicle GPS streams from emergency_gps.csv");
        System.out.println("2. Export   : Generates formatted preemption logs & performance summaries to .txt\n");

        loadGpsDataFromCsv("emergency_gps.csv");
        exportReportsToTxt();
    }

    // -------------------------------------------------------------------------
    // MODULE 7: Database Management & Persistence Audit
    // -------------------------------------------------------------------------
    public void runDatabaseModule() {
        System.out.println("\n=========================================================================");
        System.out.println("   MODULE 7: DATABASE PERSISTENCE & AUDIT (MySQL Community 8.4 / OCI)");
        System.out.println("=========================================================================");
        checkDatabaseStatus();
        saveResultsToDatabase();

        if (DBConnection.testConnection()) {
            try {
                System.out.println("\n--- [QUERY LIVE AUDIT RECORDS FROM MySQL] ---");
                List<EmergencyVehicle> list = trafficDAO.getAllEmergencyVehicles();
                System.out.printf("  [+] Found %d emergency vehicles registered in 'emergency_vehicles':\n", list.size());
                for (EmergencyVehicle ev : list) {
                    System.out.printf("    * %s (%s) | Speed: %.1f km/h | Lat: %.6f, Lon: %.6f\n",
                        ev.getVehicleId(), ev.getCategory(), ev.getSpeedKmh(), ev.getLatitude(), ev.getLongitude());
                }
            } catch (SQLException e) {
                System.err.println("[-] Error querying database: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    public void checkDatabaseStatus() {
        System.out.println("\n--- DATABASE CONNECTIVITY INSPECTION ---");
        System.out.printf("  Configured JDBC URL : %s\n", DBConnection.getJdbcUrl());
        System.out.printf("  Configured DB User  : %s\n", DBConnection.getDbUser());
        System.out.println("  Testing connection...");

        boolean reachable = DBConnection.testConnection();
        if (reachable) {
            System.out.println("  [+] SUCCESS: Connected to MySQL database successfully!");
        } else {
            System.out.println("  [-] NOTICE: Database currently offline or unreachable.");
            System.out.println("      Standalone Simulation Mode is ACTIVE. All Java core simulation,");
            System.out.println("      multithreading, priority calculation, and TXT reporting work offline!");
        }
    }

    public List<EmergencyVehicle> loadGpsDataFromCsv(String filename) {
        System.out.printf("\n--- LOADING TELEMETRY FROM CSV: %s ---\n", filename);
        File file = new File(filename);
        try {
            this.loadedVehicles = csvParser.parseCsv(file);
            System.out.printf("  [+] Successfully loaded and validated %d emergency vehicles:\n\n", loadedVehicles.size());

            System.out.printf("  %-10s | %-15s | %-14s | %-16s | %-10s | %-8s\n",
                "VEHICLE", "BEACON ID", "CATEGORY", "SEVERITY", "SPEED(km/h)", "ETA(min)");
            System.out.println("  -----------------------------------------------------------------------------------");
            for (EmergencyVehicle v : loadedVehicles) {
                System.out.printf("  %-10s | %-15s | %-14s | %-16s | %10.1f | %8.2f\n",
                    v.getVehicleId(), v.getBeaconId(), v.getCategory(), v.getSeverityCode(), v.getSpeedKmh(), v.getEtaMinutes());
            }
        } catch (Exception e) {
            System.err.printf("  [-] Error loading CSV: %s\n", e.getMessage());
        }
        return loadedVehicles;
    }

    public void runCorridorSimulation() {
        if (loadedVehicles.isEmpty()) {
            loadGpsDataFromCsv("emergency_gps.csv");
        }

        allRecordedLogs.clear();
        allRecordedReports.clear();

        System.out.printf("\n  Corridor Route Intersections: %d points\n", sampleCorridor.size());
        for (Intersection i : sampleCorridor) {
            System.out.printf("    * %s (%s) - Initial: %s, Blocked: %b\n",
                i.getIntersectionId(), i.getName(), i.getCurrentSignal(), i.isBlocked());
        }
        System.out.println("\n  Launching parallel CorridorWorker threads...\n");

        for (EmergencyVehicle v : loadedVehicles) {
            initializeDefaultIntersections();

            System.out.printf("  >>> Dispatching %s (%s, ETA: %.2f min) <<<\n",
                v.getVehicleId(), v.getCategory().getDisplayName(), v.getEtaMinutes());

            CorridorRouterService.CorridorSimulationResult result = routerService.simulateCorridor(v, sampleCorridor);
            allRecordedReports.add(result.getReport());
            allRecordedLogs.addAll(result.getLogs());

            System.out.printf("      [Result] Score: %.2f | Tier: %s | Transit: %ds | ETE: %.1f%% | CMI: %.1f%% | Status: %s\n",
                result.getReport().getPriorityScore(),
                result.getReport().getPriorityTier().getLabel(),
                result.getReport().getTotalTransitSeconds(),
                result.getReport().getEteScore(),
                result.getReport().getCmiScore(),
                result.getReport().getCorridorStatus()
            );

            System.out.println("      [Corridor Wave Map]:");
            for (PreemptionLog log : result.getLogs()) {
                String waveIcon = log.isOverrideTriggered() && log.getNewSignal() == SignalState.GREEN
                    ? "[OVERRIDE -> GREEN WAVE ACTIVE]"
                    : "[STANDARD CYCLE MAINTAINED]";
                System.out.printf("        [%s] %-6s -> %-6s | %s\n",
                    log.getIntersectionId(),
                    log.getPreviousSignal(),
                    log.getNewSignal(),
                    waveIcon
                );
            }
            System.out.println();
        }
    }

    public void saveResultsToDatabase() {
        System.out.println("\n--- PERSISTING SIMULATION RESULTS TO MYSQL ---");
        if (!DBConnection.testConnection()) {
            System.out.println("  [-] Database is currently offline. Skipping DB persistence.");
            System.out.println("      Start MySQL Community Server 8.4 and run smart_traffic_schema.sql to enable persistence.");
            return;
        }

        try (java.sql.Connection conn = DBConnection.getConnection()) {
            int vCount = 0;
            for (EmergencyVehicle v : loadedVehicles) {
                trafficDAO.insertEmergencyVehicle(conn, v);
                vCount++;
            }
            System.out.printf("  [+] Persisted %d emergency vehicles to 'emergency_vehicles' table.\n", vCount);

            int iCount = 0;
            for (Intersection i : sampleCorridor) {
                trafficDAO.insertIntersection(conn, i);
                iCount++;
            }
            System.out.printf("  [+] Persisted %d intersections to 'intersections' table.\n", iCount);

            int lCount = 0;
            for (PreemptionLog l : allRecordedLogs) {
                trafficDAO.insertPreemptionLog(conn, l);
                lCount++;
            }
            System.out.printf("  [+] Persisted %d preemption events to 'preemption_logs' table.\n", lCount);

            int rCount = 0;
            for (CorridorReport r : allRecordedReports) {
                trafficDAO.insertCorridorReport(conn, r);
                rCount++;
            }
            System.out.printf("  [+] Persisted %d corridor performance reports to 'corridor_reports' table.\n", rCount);

        } catch (SQLException e) {
            System.err.printf("  [-] Database error during persistence: %s\n", e.getMessage());
        }
    }

    public void exportReportsToTxt() {
        System.out.println("\n--- EXPORTING EXECUTION LOGS & SUMMARIES (TXT) ---");
        if (allRecordedLogs.isEmpty()) {
            System.out.println("  [!] No simulation logs recorded. Running simulation first...");
            runCorridorSimulation();
        }

        try {
            File execFile = preemptionWriter.exportExecutionLogsToTxt(allRecordedLogs);
            System.out.printf("  [+] Execution logs exported to : %s\n", execFile.getAbsolutePath());

            for (CorridorReport rep : allRecordedReports) {
                File summaryFile = preemptionWriter.exportPerformanceSummaryToTxt(rep, allRecordedLogs);
                System.out.printf("  [+] Summary report exported to : %s\n", summaryFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.printf("  [-] Error exporting reports: %s\n", e.getMessage());
        }
    }

    public void runEndToEndPipeline() {
        System.out.println("=========================================================================");
        System.out.println("          STARTING END-TO-END CORRIDOR SIMULATION PIPELINE");
        System.out.println("=========================================================================");
        checkDatabaseStatus();
        loadGpsDataFromCsv("emergency_gps.csv");
        runPriorityScoringModule();
        runCorridorSimulation();
        saveResultsToDatabase();
        exportReportsToTxt();
        System.out.println("=========================================================================");
        System.out.println("          PIPELINE EXECUTION COMPLETED SUCCESSFULLY");
        System.out.println("=========================================================================");
    }
}
