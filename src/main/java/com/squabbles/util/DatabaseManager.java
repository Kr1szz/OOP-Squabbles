package com.squabbles.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:squabbles.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS players (" +
                         "name TEXT PRIMARY KEY, " +
                         "wins INTEGER DEFAULT 0, " +
                         "losses INTEGER DEFAULT 0)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addPlayer(String name) {
        String sql = "INSERT OR IGNORE INTO players(name) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStats(String name, boolean won) {
        String sql = won ? "UPDATE players SET wins = wins + 1 WHERE name = ?" 
                         : "UPDATE players SET losses = losses + 1 WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getStats(String name) {
        String sql = "SELECT wins, losses FROM players WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Wins: " + rs.getInt("wins") + " | Losses: " + rs.getInt("losses");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "New Player";
    }
}
