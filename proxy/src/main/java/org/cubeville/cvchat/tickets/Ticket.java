package org.cubeville.cvchat.tickets;

import java.util.UUID;

public class Ticket
{
    private Integer id;

    private UUID player;
    private String playerName;
    private String text;
    private String server;
    private String world;
    private int x;
    private int y;
    private int z;
    private long creationTimestamp;

    private boolean isClosed;
    private boolean isHeld;
    private boolean isClaimed;
    private boolean playerNotified;

    private UUID moderator;
    private String moderatorName;
    private String moderatorText;
    private long moderatorTimestamp;

    public Ticket(Integer id, UUID player, String playerName, String text, String server, String world, int x, int y, int z, long creationTimestamp) {
        this.id = id;
        this.player = player;
        this.playerName = playerName;
        this.text = text;
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.creationTimestamp = creationTimestamp;
        isClaimed = false;
        isClosed = false;
        isHeld = false;
        playerNotified = false;
    }

    public Ticket(Integer id, UUID player, String playerName, String text, String server, String world, int x, int y, int z, long creationTimestamp, boolean isClosed, boolean isHeld, boolean isClaimed, boolean playerNotified, UUID moderator, String moderatorName, String moderatorText, long moderatorTimestamp) {
        this(id, player, playerName, text, server, world, x, y, z, creationTimestamp);
        this.isClosed = isClosed;
        this.isHeld = isHeld;
        this.isClaimed = isClaimed;
        this.playerNotified = playerNotified;
        this.moderator = moderator;
        this.moderatorName = moderatorName;
        this.moderatorText = moderatorText;
        this.moderatorTimestamp = moderatorTimestamp;
    }

    public void claim(UUID moderator, String moderatorName, long moderatorTimestamp) {
        this.moderator = moderator;
        this.moderatorName = moderatorName;
        this.moderatorTimestamp = moderatorTimestamp;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean closed) { isClosed = closed; }

    public boolean isHeld() { return isHeld; }
    public void setHeld(boolean held) { this.isHeld = held; }
    
    public boolean isClaimed() { return isClaimed; }
    public void setClaimed(boolean claimed) { this.isClaimed = claimed; }

    public boolean playerNotified() { return playerNotified; }
    public void setPlayerNotified(boolean playerNotified) { this.playerNotified = playerNotified; }
    
    public UUID getPlayer() { return player; }

    public String getPlayerName() { return playerName; }

    public String getText() { return text; }

    public long getCreationTimestamp() { return creationTimestamp; }

    public String getServer() { return server; }

    public String getWorld() { return world; }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getZ() { return z; }

    public void setModerator(UUID moderator) { this.moderator = moderator; }
    public UUID getModerator() { return moderator; }

    public void setModeratorName(String moderatorName) { this.moderatorName = moderatorName; }
    public String getModeratorName() { return moderatorName; }

    public void setModeratorTimestamp(long moderatorTimestamp) { this.moderatorTimestamp = moderatorTimestamp; }
    public long getModeratorTimestamp() { return moderatorTimestamp; }

    public String getModeratorText() { return moderatorText; }
    public void setModeratorText(String moderatorText) { this.moderatorText = moderatorText; }
}


