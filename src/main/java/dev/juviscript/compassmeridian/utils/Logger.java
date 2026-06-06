package dev.juviscript.compassmeridian.utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Error-only file logger.
 * Writes to %APPDATA%\Meridian\meridian.log on Windows,
 * or ~/Meridian/meridian.log on other platforms.
 * Rotates at 1MB, keeping the last 3 log files.
 */
public class Logger {

    private static final long   MAX_LOG_SIZE_BYTES = 1024 * 1024; // 1MB
    private static final int    MAX_LOG_FILES      = 3;
    private static final String LOG_DIR_NAME       = "Meridian";
    private static final String LOG_FILE_NAME      = "meridian.log";

    private static File    logFile;
    private static boolean initialized = false;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ── Init ──────────────────────────────────────────────

    public static void init() {
        try {
            File logDir = getLogDir();
            if (!logDir.exists()) logDir.mkdirs();

            logFile = new File(logDir, LOG_FILE_NAME);
            initialized = true;

            info("app", "Meridian started — log file: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[logger] Failed to initialize logger: " + e.getMessage());
        }
    }

    // ── Public API ────────────────────────────────────────

    public static void error(String tag, String message) {
        write("ERROR", tag, message, null);
    }

    public static void error(String tag, String message, Throwable throwable) {
        write("ERROR", tag, message, throwable);
    }

    public static void info(String tag, String message) {
        write("INFO", tag, message, null);
    }

    public static void warn(String tag, String message) {
        write("WARN", tag, message, null);
    }

    // ── Write ─────────────────────────────────────────────

    private static synchronized void write(String level, String tag,
                                           String message, Throwable throwable) {
        if (!initialized) return;

        try {
            rotateIfNeeded();

            String timestamp = DATE_FORMAT.format(new Date());
            String line = String.format("[%s] [%-5s] [%s] %s",
                    timestamp, level, tag, message);

            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(line);
                if (throwable != null) {
                    throwable.printStackTrace(pw);
                }
            }

        } catch (Exception e) {
            System.err.println("[logger] Failed to write log: " + e.getMessage());
        }
    }

    // ── Rotation ──────────────────────────────────────────

    private static void rotateIfNeeded() {
        if (logFile == null || !logFile.exists()) return;
        if (logFile.length() < MAX_LOG_SIZE_BYTES) return;

        File logDir = logFile.getParentFile();

        // Shift existing rotated files: .2 → .3, .1 → .2
        for (int i = MAX_LOG_FILES - 1; i >= 1; i--) {
            File older = new File(logDir, LOG_FILE_NAME + "." + i);
            File newer = new File(logDir, LOG_FILE_NAME + "." + (i + 1));
            if (older.exists()) {
                if (newer.exists()) newer.delete();
                older.renameTo(newer);
            }
        }

        // Rotate current log to .1
        File rotated = new File(logDir, LOG_FILE_NAME + ".1");
        if (rotated.exists()) rotated.delete();
        logFile.renameTo(rotated);

        // Create fresh log file
        logFile = new File(logDir, LOG_FILE_NAME);
    }

    // ── Helpers ───────────────────────────────────────────

    private static File getLogDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isEmpty()) {
            return new File(appData, LOG_DIR_NAME);
        }
        // Fallback for non-Windows
        return new File(System.getProperty("user.home"), LOG_DIR_NAME);
    }

    public static String getLogPath() {
        return logFile != null ? logFile.getAbsolutePath() : "Not initialized";
    }
}
