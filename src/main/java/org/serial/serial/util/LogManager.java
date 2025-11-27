package org.serial.serial.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class LogManager {
    private static LogManager instance;
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private LocalDate currentLogDate;
    private BufferedWriter logWriter;
    private Consumer<String> logCallback;

    private LogManager() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            openLogFile();
        } catch (IOException e) {
            System.err.println("Failed to initialize log manager: " + e.getMessage());
        }
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }

    private void openLogFile() throws IOException {
        LocalDate today = LocalDate.now();

        // Check if we need to rotate the log file
        if (currentLogDate == null || !currentLogDate.equals(today)) {
            closeLogFile();

            String fileName = String.format("app_%s.log", today.format(FILE_DATE_FORMAT));
            Path logPath = Paths.get(LOG_DIR, fileName);

            logWriter = new BufferedWriter(new FileWriter(logPath.toFile(), true));
            currentLogDate = today;

            // Clean up old log files (keep last 30 days)
            cleanupOldLogs();
        }
    }

    private void closeLogFile() {
        if (logWriter != null) {
            try {
                logWriter.close();
            } catch (IOException e) {
                System.err.println("Error closing log file: " + e.getMessage());
            }
        }
    }

    private void cleanupOldLogs() {
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(30);

            Files.list(Paths.get(LOG_DIR))
                    .filter(path -> path.toString().endsWith(".log"))
                    .filter(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            String dateStr = fileName.replace("app_", "").replace(".log", "");
                            LocalDate fileDate = LocalDate.parse(dateStr, FILE_DATE_FORMAT);
                            return fileDate.isBefore(cutoffDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Deleted old log file: " + path.getFileName());
                        } catch (IOException e) {
                            System.err.println("Failed to delete log file: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error during log cleanup: " + e.getMessage());
        }
    }

    private void log(String level, String message) {
        try {
            openLogFile(); // Check if rotation is needed

            String timestamp = LocalDateTime.now().format(LOG_TIME_FORMAT);
            String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);

            // Write to file
            if (logWriter != null) {
                logWriter.write(logEntry);
                logWriter.newLine();
                logWriter.flush();
            }

            // Call UI callback
            if (logCallback != null) {
                logCallback.accept(logEntry);
            }

            // Also print to console for debugging
            System.out.println(logEntry);

        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void debug(String message) {
        log("DEBUG", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void shutdown() {
        closeLogFile();
    }
}
