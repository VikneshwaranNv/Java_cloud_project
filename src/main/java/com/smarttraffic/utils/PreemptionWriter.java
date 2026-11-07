package com.smarttraffic.utils;

import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.PreemptionLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for formatting execution logs, formatting performance summaries,
 * and exporting results into TXT files.
 * Implements STAGE 3, STAGE 8 (String.format()), and STAGE 9.
 */
public class PreemptionWriter {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String outputDirectory;

    public PreemptionWriter() {
        this("output");
    }

    public PreemptionWriter(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Formats preemption logs into a readable text log using String.format().
     */
    public String formatExecutionLogs(List<PreemptionLog> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================================\n");
        sb.append(String.format("SMART CITY TRAFFIC ROUTER - PREEMPTION EXECUTION LOGS | %s\n", LocalDateTime.now().format(DISPLAY_TIMESTAMP)));
        sb.append("========================================================================================================\n\n");

        if (logs == null || logs.isEmpty()) {
            sb.append("No preemption events were recorded.\n");
            return sb.toString();
        }

        sb.append(String.format(
            "%-10s | %-12s | %-8s | %-10s | %-8s | %-12s | %-24s | %-10s\n",
            "VEHICLE", "INTERSECT", "ETA(MIN)", "OVERRIDE", "STATUS", "SIGNAL TRANS", "WORKER THREAD", "TRANSIT(S)"
        ));
        sb.append("--------------------------------------------------------------------------------------------------------\n");

        for (PreemptionLog log : logs) {
            String signalTransition = String.format("%s -> %s", log.getPreviousSignal(), log.getNewSignal());
            sb.append(String.format(
                "%-10s | %-12s | %8.2f | %-10b | %-8s | %-12s | %-24s | %10d\n",
                log.getVehicleId(),
                log.getIntersectionId(),
                log.getEtaMinutes(),
                log.isOverrideTriggered(),
                log.getOverrideStatus(),
                signalTransition,
                log.getThreadName(),
                log.getTransitDurationSec()
            ));
        }

        sb.append("\n--- DETAILED WORKER ACTIVITY TRACES ---\n\n");
        for (PreemptionLog log : logs) {
            sb.append(String.format("[%s @ %s]\n", log.getVehicleId(), log.getIntersectionId()));
            sb.append(log.getLogMessage());
            sb.append("\n");
        }

        sb.append("========================================================================================================\n");
        sb.append("END OF EXECUTION LOG\n");
        return sb.toString();
    }

    /**
     * Formats a corridor performance summary into a readable summary using String.format().
     */
    public String formatPerformanceSummary(CorridorReport report, List<PreemptionLog> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================================\n");
        sb.append(String.format("GREEN CORRIDOR PERFORMANCE SUMMARY - REPORT ID: %s\n", report.getReportId()));
        sb.append(String.format("Generated: %s | Operational Status: %s\n", report.getGeneratedAt().format(DISPLAY_TIMESTAMP), report.getCorridorStatus()));
        sb.append("========================================================================================================\n\n");

        sb.append("1. VEHICLE ROUTING PROFILE\n");
        sb.append("--------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("  * Vehicle Identifier   : %s\n", report.getVehicleId()));
        sb.append(String.format("  * Category Class       : %s\n", report.getCategory().getDisplayName()));
        sb.append(String.format("  * Priority Score       : %.2f / 100.00\n", report.getPriorityScore()));
        sb.append(String.format("  * Assigned Tier        : %s\n", report.getPriorityTier().getLabel()));
        sb.append(String.format("  * Max Permitted Delay  : %.1f minutes\n", report.getCategory().getMaxDelayMinutes()));

        sb.append("\n2. CORRIDOR TRANSIT METRICS & MATHEMATICAL EVALUATION\n");
        sb.append("--------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("  * Total Intersections Traversing : %d\n", report.getTotalIntersections()));
        sb.append(String.format("  * Overridden Intersections (Wave): %d\n", report.getOverriddenIntersections()));
        sb.append(String.format("  * Total Transit Elapsed Time     : %d seconds (%.2f minutes)\n",
            report.getTotalTransitSeconds(), (report.getTotalTransitSeconds() / 60.0)));
        sb.append(String.format("  * Emergency Transit Efficiency   : %.2f%% (ETE = [UncongestedTime / ActualTransit] * 100)\n",
            report.getEteScore()));
        sb.append(String.format("  * Congestion Mitigation Index    : %.2f%% (CMI = 100 - [IntersectionDelay / StandardCycle] * 100)\n",
            report.getCmiScore()));

        sb.append("\n3. EXECUTIVE SUMMARY\n");
        sb.append("--------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("  %s\n\n", report.getSummaryText()));

        sb.append("========================================================================================================\n");
        return sb.toString();
    }

    /**
     * Exports execution logs to a TXT file in the output directory.
     *
     * @param logs list of PreemptionLogs
     * @return the created File object
     * @throws IOException if writing fails
     */
    public File exportExecutionLogsToTxt(List<PreemptionLog> logs) throws IOException {
        String filename = String.format("execution_logs_%s.txt", LocalDateTime.now().format(FILE_TIMESTAMP));
        File file = new File(outputDirectory, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(formatExecutionLogs(logs));
        }
        return file;
    }

    /**
     * Exports corridor performance summary to a TXT file in the output directory.
     *
     * @param report the CorridorReport
     * @param logs the corresponding PreemptionLogs
     * @return the created File object
     * @throws IOException if writing fails
     */
    public File exportPerformanceSummaryToTxt(CorridorReport report, List<PreemptionLog> logs) throws IOException {
        String filename = String.format("corridor_performance_%s.txt", report.getVehicleId());
        File file = new File(outputDirectory, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(formatPerformanceSummary(report, logs));
        }
        return file;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }
}
