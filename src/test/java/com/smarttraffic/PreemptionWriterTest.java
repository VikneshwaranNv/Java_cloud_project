package com.smarttraffic;

import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.OverrideStatus;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.models.SignalState;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.utils.PreemptionWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for STAGE 3, 8, 9 PreemptionWriter log formatting and TXT export.
 */
public class PreemptionWriterTest {

    @Test
    @DisplayName("Format and export execution logs to TXT")
    void testExportExecutionLogs() throws IOException {
        PreemptionWriter writer = new PreemptionWriter("target/test-output");

        List<PreemptionLog> logs = new ArrayList<>();
        logs.add(new PreemptionLog(
            "EV-101", "INT-01", 1.4, true, OverrideStatus.SUCCESS,
            SignalState.RED, SignalState.GREEN, 5, 26, "CorridorWorker-01",
            "[12:00:01.123] [CorridorWorker-01] Switched to GREEN"
        ));

        String formatted = writer.formatExecutionLogs(logs);
        assertNotNull(formatted);
        assertTrue(formatted.contains("SMART CITY TRAFFIC ROUTER"));
        assertTrue(formatted.contains("EV-101"));
        assertTrue(formatted.contains("INT-01"));
        assertTrue(formatted.contains("RED -> GREEN"));

        File file = writer.exportExecutionLogsToTxt(logs);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("EV-101"));
    }

    @Test
    @DisplayName("Format and export performance summary to TXT")
    void testExportPerformanceSummary() throws IOException {
        PreemptionWriter writer = new PreemptionWriter("target/test-output");

        CorridorReport report = new CorridorReport(
            "REP-001", "EV-101", VehicleCategory.AMBULANCE, 91.0, PriorityTier.GOLD_CORRIDOR,
            3, 3, 78, "COMPLETED", "All signals cleared."
        );

        String summary = writer.formatPerformanceSummary(report, new ArrayList<>());
        assertNotNull(summary);
        assertTrue(summary.contains("Gold Corridor"));
        assertTrue(summary.contains("91.00"));

        File file = writer.exportPerformanceSummaryToTxt(report, new ArrayList<>());
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
