package org.cubeville.cvchat.playerdata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import org.cubeville.cvchat.DaoBase;

public class PlayerDataDao extends DaoBase
{
    boolean valid;
    
    public PlayerDataDao(String dbUser, String dbPassword, String dbDatabase) {
        super(dbUser, dbPassword, dbDatabase);
        valid = false;
    }

    protected synchronized Map<UUID, PlayerData> loadPlayerData() {
        Connection connection = getConnection();
        Map<UUID, PlayerData> ret = new ConcurrentHashMap<>();
        try {
            Statement statement = connection.createStatement();
            statement.execute("SELECT id, uuid, name, tutorial_finished, ban_duration, UNIX_TIMESTAMP(ban_start) AS ban_start, UNIX_TIMESTAMP(ban_issued) AS ban_issued, ban_reason, banned_by, UNIX_TIMESTAMP(first_login) AS first_login, UNIX_TIMESTAMP(last_login) AS last_login, UNIX_TIMESTAMP(last_logout) AS last_logout, ip_address, priority, prefix, display_name FROM playerdata");
            ResultSet rs = statement.getResultSet();
            while(rs.next()) {
                int id = rs.getInt("id");
                UUID playerId = UUID.fromString(rs.getString("uuid"));
                String playerName = rs.getString("name");
                boolean tutorialFinished = rs.getInt("tutorial_finished") != 0;
                Long banDuration = rs.getLong("ban_duration");
                if(rs.wasNull()) banDuration = null; else banDuration *= 1000;
                Long banStart = rs.getLong("ban_start");
                if(rs.wasNull()) banStart = null; else banStart *= 1000;
                Long banIssued = rs.getLong("ban_issued");
                if(rs.wasNull()) banIssued = null; else banIssued *= 1000;
                String banReason = rs.getString("ban_reason");
                UUID bannedBy = null;
                if(rs.getString("banned_by") != null) bannedBy = UUID.fromString(rs.getString("banned_by"));

                long firstLogin = rs.getLong("first_login") * 1000;
                long lastLogin = rs.getLong("last_login") * 1000;
                long lastLogout = rs.getLong("last_logout") * 1000;
                String ipAddress = rs.getString("ip_address");
                int priority = rs.getInt("priority");
                String prefix = rs.getString("prefix");
                String displayName = rs.getString("display_name");

                PlayerData pd = new PlayerData(id, playerId, playerName, tutorialFinished, banDuration, banStart, banIssued, banReason, bannedBy, firstLogin, lastLogin, lastLogout, ipAddress, priority, prefix, displayName);
                ret.put(playerId, pd);
            }
            valid = true;
        }
        catch(SQLException e) {
            e.printStackTrace();
            System.out.println("Could not load playerdata!");
        }
        return ret;
    }

    protected synchronized void updatePlayerData(PlayerData playerData) {
        if(!valid) {
            System.out.println("Playerdata not available.");
            return;
        }

        // TODO: async!
        Connection connection = getConnection();
        try {
            PreparedStatement statement;
            boolean n = false;
            if(playerData.getId() == null) {
                statement = connection.prepareStatement("INSERT INTO playerdata (uuid, name, tutorial_finished, ban_duration, ban_start, ban_issued, ban_reason, banned_by, first_login, last_login, last_logout, ip_address, priority, prefix, display_name) VALUES(?, ?, ?, ?, FROM_UNIXTIME(?), FROM_UNIXTIME(?), ?, ?, FROM_UNIXTIME(?), FROM_UNIXTIME(?), FROM_UNIXTIME(?), ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                n = true;
            }
            else {
                statement = connection.prepareStatement("UPDATE playerdata SET uuid = ?, name = ?, tutorial_finished = ?, ban_duration = ?, ban_start = FROM_UNIXTIME(?), ban_issued = FROM_UNIXTIME(?), ban_reason = ?, banned_by = ?, first_login = FROM_UNIXTIME(?), last_login = FROM_UNIXTIME(?), last_logout = FROM_UNIXTIME(?), ip_address = ?, priority = ?, prefix = ?, display_name = ? WHERE id = ?");
                statement.setInt(16, playerData.getId());
            }
            statement.setString(1, playerData.getPlayerId().toString());
            statement.setString(2, playerData.getName());
            statement.setInt(3, playerData.getTutorialFinished() ? 1 : 0);
            if(playerData.getBanDuration() != null) statement.setLong(4, playerData.getBanDuration() / 1000);
            else statement.setNull(4, Types.INTEGER);
            if(playerData.getBanStart() != null) statement.setLong(5, playerData.getBanStart() / 1000);
            else statement.setNull(5, Types.INTEGER);
            if(playerData.getBanIssued() != null) statement.setLong(6, playerData.getBanIssued() / 1000);
            else statement.setNull(6, Types.INTEGER);
            statement.setString(7, playerData.getBanReason());
            if(playerData.getBannedBy() != null) statement.setString(8, playerData.getBannedBy().toString());
            else statement.setNull(8, Types.VARCHAR);
            statement.setLong(9, playerData.getFirstLogin() / 1000);
            statement.setLong(10, playerData.getLastLogin() / 1000);
            statement.setLong(11, playerData.getLastLogout() / 1000);
            statement.setString(12, playerData.getIpAddress());
            statement.setInt(13, playerData.getPriority());
            statement.setString(14, playerData.getPrefix());
            statement.setString(15, playerData.getDisplayName());
            // + ID!
            statement.executeUpdate();

            if(n) {
                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                int id = rs.getInt(1);
                playerData.setId(id);
            }
            statement.close();
        }
        catch(SQLException e) {
            System.out.println("Updating playerdata failed!");
            e.printStackTrace();
            throw new RuntimeException("Updating playerdata failed!");
        }
    }
}



