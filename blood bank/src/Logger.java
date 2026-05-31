

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "resources/logs/system.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void log(String level, String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.println("[" + timestamp + "] [" + level + "] " + message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    public static void info(String message) {
        log("INFO", message);
        System.out.println("[INFO] " + message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
        System.err.println("[ERROR] " + message);
    }
    
    public static void warning(String message) {
        log("WARNING", message);
        System.out.println("[WARNING] " + message);
    }
}