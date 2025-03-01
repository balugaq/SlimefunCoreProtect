package com.balugaq.slimefuncoreprotect.api.logs;

import com.balugaq.slimefuncoreprotect.api.QueryUser;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogDao {
    private static final long THRESHOLD = 1000L;
    public static void deleteLog(String user, Timestamp time, String action, String location, String slimefunId) {
        String sql = "DELETE FROM user_logs WHERE user = ? AND time = ? AND action = ? AND location = ? AND slimefun_id = ?";
        try (Connection conn = DatabaseManager.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setTimestamp(2, time);
            pstmt.setString(3, action);
            pstmt.setString(4, location);
            pstmt.setString(5, slimefunId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertLog(String user, Timestamp time, String action, String location, String slimefunId) {
        String sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id, other_data, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setTimestamp(2, time);
            pstmt.setString(3, action);
            pstmt.setString(4, location);
            pstmt.setString(5, slimefunId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertLog(String user, Timestamp time, String action, String location, String slimefunId, String otherData) {
        String sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id, other_data, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setTimestamp(2, time);
            pstmt.setString(3, action);
            pstmt.setString(4, location);
            pstmt.setString(5, slimefunId);
            pstmt.setString(6, otherData);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static @NotNull List<LogEntry> getLogs(QueryUser user, @NotNull Map<String, String> sections) {
        StringBuilder sql = new StringBuilder("SELECT * FROM user_logs");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        // 收集所有条件
        if (sections.containsKey("user:")) {
            conditions.add("user = ?");
            params.add(sections.get("user:"));
        }
        if (sections.containsKey("time:")) {
            conditions.add("time BETWEEN ? AND ?");
            long range = getTime(sections.get("time:"));
            long now = System.currentTimeMillis();
            params.add(new Timestamp(now - range));
            params.add(new Timestamp(now));
        }
        if (sections.containsKey("action:")) {
            conditions.add("action = ?");
            params.add(sections.get("action:"));
        }
        if (sections.containsKey("location:")) {
            conditions.add("location = ?");
            params.add(sections.get("location:"));
        }
        if (sections.containsKey("slimefun_id:")) {
            conditions.add("slimefun_id = ?");
            params.add(sections.get("slimefun_id:"));
        }

        // 动态拼接WHERE
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        Debug.log("SQL: " + sql.toString());
        Debug.log("Params: " + params.toString());

        return query(user, sql.toString(), params.toArray());
    }

    public static long getTime(@NotNull String time) {
        if (time.endsWith("s")) {
            return (long) (Double.parseDouble(time.replace("s", "")) * 1000.0D);
        }
        if (time.endsWith("m")) {
            return (long) (Double.parseDouble(time.replace("m", "")) * 60000.0D);
        }
        if (time.endsWith("h")) {
            return (long) (Double.parseDouble(time.replace("h", "")) * 3600000.0D);
        }
        if (time.endsWith("d")) {
            return (long) (Double.parseDouble(time.replace("d", "")) * 86400000.0D);
        }
        return 0L;
    }

    public static @NotNull List<LogEntry> getLogsByUser(QueryUser user, String u) {
        String sql = "SELECT * FROM user_logs WHERE user = ?";
        return query(user, sql, u);
    }

    public static @NotNull List<LogEntry> getLogsByTime(QueryUser user, Timestamp time) {
        String sql = "SELECT * FROM user_logs WHERE time = ?";
        return query(user, sql, time);
    }

    public static @NotNull List<LogEntry> getLogsBetween(QueryUser user, Timestamp start, Timestamp end) {
        String sql = "SELECT * FROM user_logs WHERE time BETWEEN ? AND ?";
        return query(user, sql, start, end);
    }

    public static @NotNull List<LogEntry> getLogsByAction(QueryUser user, String action) {
        String sql = "SELECT * FROM user_logs WHERE action = ?";
        return query(user, sql, action);
    }

    public static @NotNull List<LogEntry> getLogsByLocation(QueryUser user, String location) {
        String sql = "SELECT * FROM user_logs WHERE location = ?";
        return query(user, sql, location);
    }

    public static @NotNull List<LogEntry> getLogsBySlimefunId(QueryUser user, String slimefunId) {
        String sql = "SELECT * FROM user_logs WHERE slimefun_id = ?";
        return query(user, sql, slimefunId);
    }

    public static @NotNull List<LogEntry> getAllLogs(QueryUser user) {
        String sql = "SELECT * FROM user_logs";
        return query(user, sql);
    }

    private static @NotNull List<LogEntry> query(QueryUser user, String sql, Object @NotNull ... params) {
        if (QueryUser.isOutdated(user, THRESHOLD)) {
            QueryUser.updateLastQueryTime(user);
        } else {
            user.sendMessage("You can only query logs once every " + THRESHOLD / 1000 + " seconds.");
            return new ArrayList<>();
        }

        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DatabaseManager.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LogEntry log = new LogEntry();
                    log.setId(rs.getInt("id"));
                    log.setPlayer(rs.getString("user"));
                    log.setTime(rs.getLong("time"));
                    log.setAction(rs.getString("action"));
                    log.setLocation(rs.getString("location"));
                    log.setSlimefunId(rs.getString("slimefun_id"));
                    log.setOtherData(rs.getString("other_data"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs.reversed();
    }
}