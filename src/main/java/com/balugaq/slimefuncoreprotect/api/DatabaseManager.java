package com.balugaq.slimefuncoreprotect.api;

import com.balugaq.slimefuncoreprotect.api.enums.DatabaseType;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

@Getter
public class DatabaseManager {
    private static final String UNDEFINED = "UNDEFINED";
    @Getter
    private static HikariDataSource dataSource;

    static {
        try {
            DatabaseType dbType = SlimefunCoreProtect.getInstance().getConfigManager().getDatabaseType();
            HikariConfig config = new HikariConfig();

            if (dbType == DatabaseType.MYSQL) {
                FileConfiguration configuration = SlimefunCoreProtect.getInstance().getConfigManager().getConfig();
                String host = configuration.getString("database.mysql.host", "localhost");
                int port = configuration.getInt("database.mysql.port", 3306);
                String database = configuration.getString("database.mysql.database", "logs_db");
                String username = configuration.getString("database.mysql.username", UNDEFINED);
                String password = configuration.getString("database.mysql.password", UNDEFINED);
                boolean useSSL = configuration.getBoolean("database.mysql.useSSL", false);
                String timezone = configuration.getString("database.mysql.timezone", "Shanghai");
                if (username.equals(UNDEFINED) || password.equals(UNDEFINED)) {
                    throw new IllegalArgumentException("MySQL username or password is undefined.");
                }
                int maxConnections = configuration.getInt("database.mysql.maxConnections", 10);
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&serverTimezone=" + timezone;
                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setMaximumPoolSize(maxConnections);
            } else if (dbType == DatabaseType.SQLITE) {
                int maxConnections = SlimefunCoreProtect.getInstance().getConfigManager().getConfig().getInt("database.sqlite.maxConnections", 5);
                String filePath = SlimefunCoreProtect.getInstance().getDataFolder().toPath() + "/logs.db";
                Debug.log("Using SQLite database at " + filePath);
                config.setJdbcUrl("jdbc:sqlite:" + filePath);
                config.setDriverClassName("org.sqlite.JDBC");
                config.setMaximumPoolSize(maxConnections);
            } else {
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
            }

            dataSource = new HikariDataSource(config);
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertLog(LogEntry logEntry) {
        insertLog(logEntry.getUser(), logEntry.getTime(), logEntry.getAction(), logEntry.getLocation());
    }

    public static void insertLog(String user, Timestamp timestamp, String action, @NotNull Location location) {
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setTimestamp(2, timestamp);
            stmt.setString(3, action);
            stmt.setString(4, LogEntry.getStringBlockLocation(location));
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
            timestamp = Timestamp.valueOf(time);
        } catch (IllegalArgumentException e) {
            Debug.log("Invalid timestamp: " + time);
            return;
        }

        if (timestamp == null) {
            Debug.log("Invalid timestamp: " + time);
            return;
        }

        try (Connection conn = dataSource.getConnection();
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
        if (dataSource != null) {
            dataSource.close();
        }
    }
}