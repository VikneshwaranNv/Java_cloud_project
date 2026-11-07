package com.smarttraffic;

import com.smarttraffic.database.DBConnection;
import com.smarttraffic.database.TrafficDAO;
import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.SignalState;
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
 * Implements STAGE 3, STAGE 5, STAGE 6, STAGE 7, STAGE 9, and STAGE 10.
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

        // Check if non-interactive mode flag is passed (e.g. for CI or automated demo)
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
            System.out.print("Select an option [1-8]: ");
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "8";

            switch (input) {
                case "1":
                    checkDatabaseStatus();
                    break;
                case "2":
                    loadGpsDataFromCsv("emergency_gps.csv");
                    break;
                case "3":
                    runPriorityScoring();
                    break;
                case "4":
                    runCorridorSimulation();
                    break;
                case "5":
                    saveResultsToDatabase();
                    break;
                case "6":
                    exportReportsToTxt();
                    break;
                case "7":
                    runEndToEndPipeline();
                    break;
                case "8":
                    System.out.println("\nExiting Smart City Traffic Router. Thank you!");
                    exit = true;
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please choose an option from 1 to 8.");
            }

            if (!exit) {
                System.out.println("\nPress Enter to continue...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
        }
    }

    private void printBanner() {
        System.out.println("=========================================================================================");
        System.out.println("     SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER");
        System.out.println("     Student 1: Java 21 Application Development & Local Database Integration");
        System.out.println("=========================================================================================");
    }

    private void printMenu() {
        System.out.println("\n--------------- MAIN APPLICATION MENU ---------------");
        System.out.println("1. Test Database Connectivity (Local MySQL / OCI Remote)");
        System.out.println("2. Load & Validate Emergency GPS Telemetry (CSV)");
        System.out.println("3. Calculate Category-Specific Priority Scores & Tiers");
        System.out.println("4. Execute Multithreaded Green Corridor Simulation");
        System.out.println("5. Persist Simulation Results to MySQL Database");
        System.out.println("6. Export Execution Logs & Performance Summaries (TXT)");
        System.out.println("7. Run Complete Automated End-to-End Pipeline");
        System.out.println("8. Exit Application");
        System.out.println("-----------------------------------------------------");
    }

    public void checkDatabaseStatus() {
        System.out.println("\n--- [1] DATABASE CONNECTIVITY INSPECTION ---");
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
        System.out.printf("\n--- [2] LOADING TELEMETRY FROM CSV: %s ---\n", filename);
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

    public void runPriorityScoring() {
        System.out.println("\n--- [3] EMERGENCY VEHICLE PRIORITY SCORING & TIER EVALUATION ---");
        if (loadedVehicles.isEmpty()) {
            System.out.println("  [!] No vehicles loaded. Loading default emergency_gps.csv...");
            loadGpsDataFromCsv("emergency_gps.csv");
        }

        System.out.printf("  %-10s | %-16s | %-15s | %-8s | %-22s\n",
            "VEHICLE", "CATEGORY", "FORMULA INPUTS", "SCORE", "ASSIGNED TIER");
        System.out.println("  ----------------------------------------------------------------------------------");

        for (EmergencyVehicle v : loadedVehicles) {
            double score = priorityCalculator.calculateScore(v);
            // Default check with override assumed true for baseline scoring
            priorityCalculator.assignTier(v, true);

            String inputs = String.format("%.0f, %.0f, %.0f", v.getParam1(), v.getParam2(), v.getParam3());
            System.out.printf("  %-10s | %-16s | %-15s | %8.2f | %-22s\n",
                v.getVehicleId(), v.getCategory().getDisplayName(), inputs, score, v.getPriorityTier().getLabel());
        }
    }

    public void runCorridorSimulation() {
        System.out.println("\n--- [4] MULTITHREADED GREEN CORRIDOR SIMULATION ---");
        if (loadedVehicles.isEmpty()) {
            loadGpsDataFromCsv("emergency_gps.csv");
        }

        allRecordedLogs.clear();
        allRecordedReports.clear();

        System.out.printf("  Corridor Route Intersections: %d points\n", sampleCorridor.size());
        for (Intersection i : sampleCorridor) {
            System.out.printf("    * %s (%s) - Initial: %s, Blocked: %b\n",
                i.getIntersectionId(), i.getName(), i.getCurrentSignal(), i.isBlocked());
        }
        System.out.println("\n  Launching parallel CorridorWorker threads...\n");

        for (EmergencyVehicle v : loadedVehicles) {
            // Reset corridor state before each vehicle's run
            initializeDefaultIntersections();

            System.out.printf("  >>> Dispatching %s (%s, ETA: %.2f min) <<<\n",
                v.getVehicleId(), v.getCategory().getDisplayName(), v.getEtaMinutes());

            CorridorRouterService.CorridorSimulationResult result = routerService.simulateCorridor(v, sampleCorridor);
            allRecordedReports.add(result.getReport());
            allRecordedLogs.addAll(result.getLogs());

            System.out.printf("      [Result] Score: %.2f | Tier: %s | Transit: %ds | Overrides: %d/%d | Status: %s\n\n",
                result.getReport().getPriorityScore(),
                result.getReport().getPriorityTier().getLabel(),
                result.getReport().getTotalTransitSeconds(),
                result.getReport().getOverriddenIntersections(),
                result.getReport().getTotalIntersections(),
                result.getReport().getCorridorStatus()
            );
        }
    }

    public void saveResultsToDatabase() {
        System.out.println("\n--- [5] PERSISTING SIMULATION RESULTS TO MYSQL ---");
        if (!DBConnection.testConnection()) {
            System.out.println("  [-] Database is currently offline. Skipping DB persistence.");
            System.out.println("      Start MySQL Community Server 8.4 and run smart_traffic_schema.sql to enable persistence.");
            return;
        }

        try {
            int vCount = 0;
            for (EmergencyVehicle v : loadedVehicles) {
                trafficDAO.insertEmergencyVehicle(v);
                vCount++;
            }
            System.out.printf("  [+] Persisted %d emergency vehicles to 'emergency_vehicles' table.\n", vCount);

            int iCount = 0;
            for (Intersection i : sampleCorridor) {
                trafficDAO.insertIntersection(i);
                iCount++;
            }
            System.out.printf("  [+] Persisted %d intersections to 'intersections' table.\n", iCount);

            int lCount = 0;
            for (PreemptionLog l : allRecordedLogs) {
                trafficDAO.insertPreemptionLog(l);
                lCount++;
            }
            System.out.printf("  [+] Persisted %d preemption events to 'preemption_logs' table.\n", lCount);

            int rCount = 0;
            for (CorridorReport r : allRecordedReports) {
                trafficDAO.insertCorridorReport(r);
                rCount++;
            }
            System.out.printf("  [+] Persisted %d corridor performance reports to 'corridor_reports' table.\n", rCount);

        } catch (SQLException e) {
            System.err.printf("  [-] Database error during persistence: %s\n", e.getMessage());
        }
    }

    public void exportReportsToTxt() {
        System.out.println("\n--- [6] EXPORTING EXECUTION LOGS & SUMMARIES (TXT) ---");
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
        runPriorityScoring();
        runCorridorSimulation();
        saveResultsToDatabase();
        exportReportsToTxt();
        System.out.println("=========================================================================");
        System.out.println("          PIPELINE EXECUTION COMPLETED SUCCESSFULLY");
        System.out.println("=========================================================================");
    }

    // Getters for testing
    public List<EmergencyVehicle> getLoadedVehicles() {
        return loadedVehicles;
    }

    public List<PreemptionLog> getAllRecordedLogs() {
        return allRecordedLogs;
    }

    public List<CorridorReport> getAllRecordedReports() {
        return allRecordedReports;
    }
}
