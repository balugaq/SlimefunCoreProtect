package com.balugaq.slimefuncoreprotect.api.logs;

import com.balugaq.slimefuncoreprotect.api.enums.DatabaseType;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public final class SourceManager {
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
            } else if (dbType == DatabaseType.POSTGRESQL) {
                FileConfiguration configuration = SlimefunCoreProtect.getInstance().getConfigManager().getConfig();
                String host = configuration.getString("database.postgresql.host", "localhost");
                int port = configuration.getInt("database.postgresql.port", 5432);
                String database = configuration.getString("database.postgresql.database", "logs_db");
                String username = configuration.getString("database.postgresql.username", UNDEFINED);
                String password = configuration.getString("database.postgresql.password", UNDEFINED);
                boolean useSSL = configuration.getBoolean("database.postgresql.useSSL", false);

                if (username.equals(UNDEFINED) || password.equals(UNDEFINED)) {
                    throw new IllegalArgumentException("PostgreSQL username or password is undefined.");
                }

                int maxConnections = configuration.getInt("database.postgresql.maxConnections", 10);
                String url = "jdbc:postgresql://" + host + ":" + port + "/" + database +
                        "?ssl=" + useSSL +
                        "&sslmode=" + (useSSL ? "require" : "disable");

                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(maxConnections);
            } else {
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
            }

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
