package com.balugaq.slimefuncoreprotect.api.logs;

import com.balugaq.slimefuncoreprotect.api.enums.DatabaseType;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

@Getter
public class BlockDatabaseManager {
    @Getter
    public static DataSource dataSource = SourceManager.getDataSource();
    private static void createTable() {
        DatabaseType dbType = SlimefunCoreProtect.getInstance().getConfigManager().getDatabaseType();
        String sql = null;
        if (dbType == DatabaseType.SQLITE) {
            sql = """
                    CREATE TABLE IF NOT EXISTS user_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user TEXT,
                        time TEXT,
                        action TEXT,
                        location TEXT,
                        slimefun_id TEXT,
                        status INTEGER DEFAULT 0
                    )
                    """;
        } else if (dbType == DatabaseType.MYSQL){
            sql = """
                    CREATE TABLE IF NOT EXISTS user_logs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user VARCHAR(255),
                        time TIMESTAMP,
                        action VARCHAR(255),
                        location VARCHAR(255),
                        slimefun_id VARCHAR(255),
                        status INT DEFAULT 0
                    )
                    """;
        }
        if (sql == null) {
            Debug.log("Unsupported database type: " + dbType);
        }
        try (Connection conn = SourceManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertLog(@NotNull LogEntry logEntry) {
        insertLog(logEntry.getPlayer(), logEntry.getTime(), logEntry.getAction(), logEntry.getLocation(), logEntry.getSlimefunId());
    }

    public static void insertLog(String user, Timestamp timestamp, String action, @NotNull Location location, String slimefunId) {
        DatabaseType dbType = SlimefunCoreProtect.getInstance().getConfigManager().getDatabaseType();
        String sql = null;
        if (dbType == DatabaseType.SQLITE) {
            sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id, status) VALUES (?,?,?,?,?,?)";
        } else if (dbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id, status) VALUES (?, ?, ?, ?, ?, ?)";
        }
        if (sql == null) {
            Debug.log("Unsupported database type: " + dbType);
            return;
        }
        try (Connection conn = SourceManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setTimestamp(2, timestamp);
            stmt.setString(3, action);
            stmt.setString(4, LogEntry.getStringBlockLocation(location));
            stmt.setString(5, slimefunId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertLog(String user, @NotNull String time, String action, String location) {
        DatabaseType dbType = SlimefunCoreProtect.getInstance().getConfigManager().getDatabaseType();
        String sql = null;
        if (dbType == DatabaseType.SQLITE) {
            sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id, status) VALUES (?,?,?,?,?,?)";
        } else if (dbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id, status) VALUES (?, ?, ?, ?, ?, ?)";
        }
        if (sql == null) {
            Debug.log("Unsupported database type: " + dbType);
            return;
        }
        Timestamp timestamp = null;
        try {
            timestamp = Timestamp.valueOf(time); // yyyy-[M]M-[d]d hh:mm:ss
        } catch (IllegalArgumentException e) {
            Debug.log("Invalid timestamp: " + time);
            return;
        }

        if (timestamp == null) {
            Debug.log("Invalid timestamp: " + time);
            return;
        }

        try (Connection conn = SourceManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setTimestamp(2, timestamp);
            stmt.setString(3, action);
            stmt.setString(4, location);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        SourceManager.shutdown();
    }
}