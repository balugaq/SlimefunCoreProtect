package com.balugaq.slimefuncoreprotect.api;

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
        String sql = "INSERT INTO user_logs (user, time, action, location, slimefun_id) VALUES (?, ?, ?, ?, ?)";
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

    public static @NotNull List<LogEntry> getLogsByUser(String user) {
        String sql = "SELECT * FROM user_logs WHERE user = ?";
        return query(sql, user);
    }

    public static @NotNull List<LogEntry> getLogsByTime(Timestamp time) {
        String sql = "SELECT * FROM user_logs WHERE time = ?";
        return query(sql, time);
    }

    public static List<LogEntry> getLogsBetween(Timestamp start, Timestamp end) {
        String sql = "SELECT * FROM user_logs WHERE time BETWEEN ? AND ?";
        return query(sql, start, end);
    }

    public static @NotNull List<LogEntry> getLogsByAction(String action) {
        String sql = "SELECT * FROM user_logs WHERE action = ?";
        return query(sql, action);
    }

    public static @NotNull List<LogEntry> getLogsByLocation(String location) {
        String sql = "SELECT * FROM user_logs WHERE location = ?";
        return query(sql, location);
    }

    public static @NotNull List<LogEntry> getLogsBySlimefunId(String slimefunId) {
        String sql = "SELECT * FROM user_logs WHERE slimefun_id = ?";
        return query(sql, slimefunId);
    }

    public static @NotNull List<LogEntry> getAllLogs() {
        String sql = "SELECT * FROM user_logs";
        return query(sql);
    }

    private static @NotNull List<LogEntry> query(String sql, Object @NotNull ... params) {
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
                    log.setUser(rs.getString("user"));
                    log.setTime(rs.getLong("time"));
                    log.setAction(rs.getString("action"));
                    log.setLocation(rs.getString("location"));
                    log.setSlimefunId(rs.getString("slimefun_id"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}