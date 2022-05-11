package org.cubeville.cvchat.playerdata;

import java.util.UUID;

public class PlayerData
{
    private Integer id;
    private UUID playerId;
    private String name;
    private boolean tutorialFinished;
    private Long banDuration;
    private Long banStart;
    private Long banIssued;
    private String banReason;
    private UUID bannedBy;
    private long firstLogin;
    private long lastLogin;
    private long lastLogout;
    private String ipAddress;
    private int priority;
    private String prefix;
    private String displayName = null;
    
    public PlayerData(UUID playerId, String name, String ipAddress) {
        id = null;
        this.playerId = playerId;
        this.name = name;
        tutorialFinished = false;
        banDuration = null;
        banStart = null;
        banIssued = null;
        banReason = null;
        bannedBy = null;
        firstLogin = System.currentTimeMillis();
        lastLogin = firstLogin;
        lastLogout = lastLogin;
        this.ipAddress = ipAddress;
        priority = 0;
        prefix = "";
        displayName = null;
    }

    public PlayerData(Integer id, UUID playerId, String name, boolean tutorialFinished, Long banDuration, Long banStart, Long banIssued, String banReason, UUID bannedBy, long firstLogin, long lastLogin, long lastLogout, String ipAddress, int priority, String prefix, String displayName) {
        this.id = id;
        this.playerId = playerId;
        this.name = name;
        this.tutorialFinished = tutorialFinished;
        this.banDuration = banDuration;
        this.banStart = banStart;
        this.banIssued = banIssued;
        this.banReason = banReason;
        this.bannedBy = bannedBy;
        this.firstLogin = firstLogin;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.ipAddress = ipAddress;
        this.priority = priority;
        this.prefix = prefix;
        this.displayName = displayName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean getTutorialFinished() {
        return tutorialFinished;
    }

    public void setTutorialFinished(boolean tutorialFinished) {
        this.tutorialFinished = tutorialFinished;
    }
    
    public Long getBanDuration() {
        return banDuration;
    }

    public void setBanDuration(Long banDuration) {
        this.banDuration = banDuration;
    }
    
    public Long getBanStart() {
        return banStart;
    }

    public void setBanStart(Long banStart) {
        this.banStart = banStart;
    }
    
    public Long getBanIssued() {
        return banIssued;
    }

    public void setBanIssued(Long banIssued) {
        this.banIssued = banIssued;
    }
    
    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
    
    public UUID getBannedBy() {
        return bannedBy;
    }

    public void setBannedBy(UUID bannedBy) {
        this.bannedBy = bannedBy;
    }

    public long getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(long firstLogin) {
        this.firstLogin = firstLogin;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public long getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(long lastLogout) {
        this.lastLogout = lastLogout;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
