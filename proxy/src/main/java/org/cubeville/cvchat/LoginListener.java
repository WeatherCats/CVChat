package org.cubeville.cvchat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.cubeville.cvchat.channels.ChannelManager;
import org.cubeville.cvchat.playerdata.PlayerDataManager;
import org.cubeville.cvchat.ranks.RankManager;
import org.cubeville.cvchat.tickets.TicketManager;

public class LoginListener implements Listener
{
    ChannelManager channelManager;
    TicketManager ticketManager;

    Map<UUID, Long> newPlayerLogins;
    boolean newPlayerBlocker = false;
    
    Set<UUID> versionCheckBypass = new HashSet<>();

    Map<String, Set<UUID>> playerIP = new HashMap<>();
    Map<UUID, String> confirmationIP = new HashMap<>();
    
    public LoginListener(ChannelManager channelManager, TicketManager ticketManager, Set<UUID> versionCheckBypass) {
        this.channelManager = channelManager;
        this.ticketManager = ticketManager;
        newPlayerLogins = new HashMap<>();

        this.versionCheckBypass = versionCheckBypass;
    }

    public void addVersionCheckBypass(UUID player) {
        versionCheckBypass.add(player);
    }
    
    @EventHandler
    public void onPreLogin(final PreLoginEvent event) {
        String playerName = event.getConnection().getName();
        Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
        for(ProxiedPlayer p: players) {
            if(playerName.equals(p.getName())) {
                event.setCancelled(true);
                event.setCancelReason("§cYou're already logged in to this server.");
                return;
            }
        }
    }

    private int newPlayerLoginCount(int Time) {
        int ret = 0;
        long currentTime = System.currentTimeMillis();
        for(UUID p: newPlayerLogins.keySet()) {
            long tdiff = currentTime - newPlayerLogins.get(p);
            if(tdiff < Time) ret++;
        }
        return ret;
    }
    
    @EventHandler
    public void onLogin(final LoginEvent event) {
        PlayerDataManager pdm = PlayerDataManager.getInstance();
        PendingConnection connection = event.getConnection();
        UUID uuid = connection.getUniqueId();

        {
            int forcedProtocolVersion = 754;
            int protocolVersion = connection.getVersion();
            if(protocolVersion != forcedProtocolVersion && versionCheckBypass.contains(uuid) == false) {
                event.setCancelled(true);
                event.setCancelReason("§cPlease use §aMinecraft v1.16.5 §cfor Cubeville.\nhttp://cubeville.org");
                return;
            }
        }
        
        if(!pdm.isPlayerKnown(uuid)) {
            // Count number of new players in last two minutes
            int c2min = newPlayerLoginCount(120000);
            int c10min = newPlayerLoginCount(600000);
            if(c2min >= 4 || c10min >= 6 || newPlayerBlocker == true) {
                if(newPlayerBlocker) {
                    System.out.println(connection.getName() + " blocked from logging on. Cause: New player blocker.");
                }
                else {
                    System.out.println(connection.getName() + " blocked from logging on. Cause: Limiter (2 minutes: " + c2min + ", 10 minutes: " + c10min);
                }
                event.setCancelled(true);
                event.setCancelReason("§cSorry, all login slots are currently occupied.\n§cPlease try again in a few minutes.");
                return;
            }
        }

        if(pdm.isBanned(uuid, true)) {
            event.setCancelled(true);
            boolean perm = pdm.isPermanentlyBanned(connection.getUniqueId());
            String type = perm ? "permanently" : "temporarily";
            String endOfBan = "";
            if(!perm) {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss z");
                endOfBan = " until §e" + sdf.format(new Date(pdm.getEndOfTempban(uuid)));
            }
            event.setCancelReason("§cYou're " + type + " banned from this server" + endOfBan + ".\n§cReason: §e" + pdm.getBanReason(uuid) + "\n§aMore Information: §eYou have been banned because a staff member has decided that you didn't play according to the Cubeville rules. If you think this is a mistake, or if you want to join the server again without violating the rules again, you can apply for an unban. For more information, visit §acubeville.org/unban§e.");
            return;
        }

        String ip = getStrippedIpAddress(connection);
        if(pdm.needsConfirmation(uuid, ip)) {
            boolean ipConfirmValid = false;
            {
                File confirmation = new File("/var/www/2falogin/players/ip" + uuid.toString());
                if(confirmation.exists()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(confirmation));
                        try {
                            String line = br.readLine();
                            if(line != null && line.equals(ip)) {
                                System.out.println("Login verification: " + ip + " equals " + line);
                                ipConfirmValid = true;
                            }
                            else {
                                System.out.println("Login verification: " + ip + " does not equal " + line);
                            }
                        }
                        finally {
                            br.close();
                        }
                    }
                    catch(Exception e) {
                        // couldn't care less
                    }
                }
            }
            if(ipConfirmValid == false) {
                if(confirmationIP.get(uuid) == null || confirmationIP.get(uuid).equals(ip) == false) {
                    File confirmation = new File("/var/www/2falogin/players/" + uuid.toString());
                    long period = 1000000;
                    if(confirmation.exists()) {
                        period = System.currentTimeMillis() - confirmation.lastModified();
                    }
                    if(period > 300000) {
                        event.setCancelled(true);
                        event.setCancelReason("§cPermission denied.");
                        return;
                    }
                    confirmationIP.put(uuid, ip);
                }
            }
        }

    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(final PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        channelManager.playerLogin(player);
        
        PlayerDataManager pdm = PlayerDataManager.getInstance();
        UUID playerId = player.getUniqueId();

        boolean newPlayer = false;

        String ip = getStrippedIpAddress(player);
        
        if(!player.hasPermission("cvchat.silent.join")) {
            if(!pdm.isPlayerKnown(playerId)) {
                newPlayer = true;
                //sendWelcomeMessage(player.getName());
                pdm.addPlayer(playerId, player.getName(), ip);
                sendPublicMessage(player.getDisplayName(), "joined", true);
                newPlayerLogins.put(playerId, System.currentTimeMillis());
            }
            else {
                if(pdm.getPlayerName(playerId).equals(player.getName())) {
                    sendPublicMessage(player.getDisplayName(), "joined", false);
                }
                else {
                    sendPublicMessage(player.getDisplayName() + " (formerly known as " + pdm.getPlayerName(playerId) + ")", "joined", false);
                    pdm.changePlayerName(playerId, player.getName());
                }
            }

            if(!player.hasPermission("cvchat.notifyipmatch.exempt")) {
                if(playerIP.containsKey(ip)) {
                    List<String> names = new ArrayList<>();
                    List<Boolean> permbanned = new ArrayList<>();
                    List<Boolean> tempbanned = new ArrayList<>();
                    for(UUID pid: playerIP.get(ip)) {
                        if(!pid.equals(playerId)) {
                            names.add(pdm.getPlayerName(pid));
                            boolean temp = pdm.isBanned(pid, false);
                            tempbanned.add(temp);
                            permbanned.add(temp && pdm.isPermanentlyBanned(pid));
                        }
                    }
                    if(names.size() > 0) {
                        sendIPMatchMessage(player.getName(), names, permbanned, tempbanned);
                    }
                    playerIP.get(ip).add(playerId);
                }
                else {
                    Set<UUID> pllist = new HashSet<>();
                    pllist.add(playerId);
                    playerIP.put(ip, pllist);
                }
            }
        }
        else {
            sendSilentMessage(player.getDisplayName(), "joined");
            if(!pdm.isPlayerKnown(playerId)) {
                pdm.addPlayer(playerId, player.getName(), ip);
            }
            else if(!pdm.getPlayerName(playerId).equals(player.getName())) {
                pdm.changePlayerName(playerId, player.getName());
            }
        }
        pdm.playerLogin(playerId, ip, RankManager.getInstance().getPriority(player));

        ticketManager.playerLogin(player);
        
        if(!pdm.finishedTutorial(playerId)) {
            channelManager.getIPC().sendMessage("cv7tutorial", "xwportal|" + playerId + "|portal:TutorialSpawn|cv7tutorial");
        }

        if(player.hasPermission("cvchat.ticket")) {
            int cnt = ticketManager.getNumberOfOpenTickets();
            if(cnt > 0) {
                player.sendMessage("§a" + cnt + " open modreq(s).");
            }
        }

        if(!RankManager.getInstance().isPrefixPermitted(player, pdm.getPrefix(playerId))) {
            pdm.changePrefix(playerId, "");
        }

        System.out.println("Player " + player.getName() + " logged in" + (newPlayer ? " for the first time: " : ": ") + ip);

        // if(users115.contains(player.getName())) {
        //     int protocolVersion = player.getPendingConnection().getVersion();
        //     if(protocolVersion == 575 && player.getReconnectServer().getName().equals("t115") == false) {
        //         player.connect(ProxyServer.getInstance().getServerInfo("t115"));
        //     }
        //     else if(protocolVersion == 340 && player.getReconnectServer().getName().equals("t115")) {
        //         player.connect(ProxyServer.getInstance().getServerInfo("survival"));
        //     }
        // }
    }

    private String getStrippedIpAddress(Connection player) {
        String ret = player.getAddress().getAddress().toString();
        ret = ret.substring(ret.indexOf("/") + 1);
        return ret;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDisconnect(final PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        channelManager.playerDisconnect(player);
        if(!player.hasPermission("cvchat.silent.leave")) {
            sendPublicMessage(player.getDisplayName(), "left", false);
        }
        else {
            sendSilentMessage(player.getDisplayName(), "left");
        }
        PlayerDataManager.getInstance().playerLogout(player.getUniqueId());
        System.out.println("Player " + player.getName() + " logged off.");
    }

    @EventHandler
    public void onProxyPing(final ProxyPingEvent event) {
        ServerPing ping = event.getResponse();
        int cnt = 0;
        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()) {
            if(!Util.playerIsUnlisted(player)) {
                cnt++;
            }
        }
        ping.getPlayers().setOnline(cnt);
        ping.getPlayers().setSample(new ServerPing.PlayerInfo[0]);
    }
    
    private void sendPublicMessage(String playerName, String status, boolean newPlayer) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(newPlayer == true && p.hasPermission("cvchat.informnewplayer")) {
                p.sendMessage("§e" + playerName + "§e " + status + " the game. §a(New player)");
            }
            else {
                p.sendMessage("§e" + playerName + "§e " + status + " the game.");
            }
        }
    }

    private void sendSilentMessage(String playerName, String status) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.silent.view")) {
                p.sendMessage("§3" + playerName + "§3 " + status + " the game silently.");
            }
        }
    }

    private void sendWelcomeMessage(String playerName) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            p.sendMessage("§eEveryone welcome Cubeville's newest player, " + playerName + "§e!");
        }        
    }

    private void sendIPMatchMessage(String name, List<String> names, List<Boolean> permbanned, List<Boolean> tempbanned) {
        String message = "";
        for(int i = 0; i < names.size(); i++) {
            if(message.length() > 0) message += "§2, ";
            if(permbanned.get(i)) {
                message += "§c";
            }
            else if(tempbanned.get(i)) {
                message += "§e";
            }
            else {
                message += "§a";
            }
            message += names.get(i);
        }
        message = "§2" + name + " could also be: §a" + message + "§2.";
        
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.notifyipmatch")) {
                p.sendMessage(message);
            }
        }
    }
    public void setNewPlayerBlocker(boolean active) {
        newPlayerBlocker = active;
    }
}
