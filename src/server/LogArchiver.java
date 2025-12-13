package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogArchiver {

    private final ScheduledExecutorService scheduler;
    private final String dbUrl;
    private final int intervalSeconds;
    private final String logFilePath;

    public LogArchiver(String dbUrl, String logFilePath, int intervalSeconds) {
        this.dbUrl = dbUrl;
        this.logFilePath = logFilePath;
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::archiveLogs, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void archiveLogs() {
        System.out.println("Archiving logs to database");

        List<String> logsToArchive = new ArrayList<>();

        // Read log lines from file
        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                logsToArchive.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Insert log lines into database
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO logs (log_date, level, section, message) VALUES (?, ?, ?, ?)"
             )) {

            for (String logLine : logsToArchive) {
                String[] parts = parseLogLine(logLine);

                pstmt.setString(1, parts[0]);
                pstmt.setString(2, parts[1]);
                pstmt.setString(3, parts[2]);
                pstmt.setString(4, parts[3]);
                pstmt.addBatch();
            }

            pstmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Parse log line into its components
    public String[] parseLogLine(String logLine) {
        try {

            String[] result = new String[4];
            result[0] = logLine.substring(0, 19);

            int levelStart = logLine.indexOf("[") + 1;
            int levelEnd = logLine.indexOf("]");
            result[1] = logLine.substring(levelStart, levelEnd);

            int sectionStart = logLine.indexOf("[", levelEnd) + 1;
            int sectionEnd = logLine.indexOf("]", sectionStart);
            result[2] = logLine.substring(sectionStart, sectionEnd);

            result[3] = logLine.substring(sectionEnd + 2);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void shutdown() {
        System.out.println("Shutting down LogArchiver...");
        scheduler.shutdownNow();
    }
}


