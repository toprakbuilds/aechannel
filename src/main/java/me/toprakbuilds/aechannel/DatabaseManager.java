package me.toprakbuilds.aechannel;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final AEChannel plugin;

    public DatabaseManager(AEChannel plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        try {
            File folder = plugin.getDataFolder();
            if (!folder.exists()) folder.mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(folder, "data.db"));
            try (Statement s = connection.createStatement()) {
                // Tablo yapısı: all_global, see_global, see_trade, mute_end, mute_reason
                s.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, all_global INT, see_global INT, see_trade INT, mute_end LONG, mute_reason TEXT)");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void load(UUID uuid, PlayerSettings s) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                s.allGlobal = rs.getInt("all_global") == 1;
                s.seeGlobal = rs.getInt("see_global") == 1;
                s.seeTrade = rs.getInt("see_trade") == 1;
                s.muteEnd = rs.getLong("mute_end");
                s.muteReason = rs.getString("mute_reason") != null ? rs.getString("mute_reason") : "";
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void save(UUID uuid, PlayerSettings s) {
        try (PreparedStatement ps = connection.prepareStatement("REPLACE INTO players (uuid, all_global, see_global, see_trade, mute_end, mute_reason) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, s.allGlobal ? 1 : 0);
            ps.setInt(3, s.seeGlobal ? 1 : 0);
            ps.setInt(4, s.seeTrade ? 1 : 0);
            ps.setLong(5, s.muteEnd);
            ps.setString(6, s.muteReason);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}