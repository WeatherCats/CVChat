package org.cubeville.cvchat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import net.md_5.bungee.util.CaseInsensitiveMap;
import org.cubeville.cvchat.channels.ChannelManager;
import org.cubeville.cvchat.ranks.RankManager;
import org.cubeville.cvchat.tickets.TicketManager;
import org.cubeville.cvplayerdata.playerdata.NameRecord;
import org.cubeville.cvplayerdata.playerdata.NameRecordDao;
import org.cubeville.cvplayerdata.playerdata.PlayerDataManager;
import org.yaml.snakeyaml.Yaml;

public class LoginListener implements Listener
{
    JsonHandler jsonHandler;
    ChannelManager channelManager;
    TicketManager ticketManager;

    Map<UUID, Long> newPlayerLogins;
    boolean newPlayerBlocker = false;
    
    Set<UUID> versionCheckBypass;

    Map<String, LinkedHashMap<String, String>> playerIPInfo = new HashMap<>();
    Map<String, Set<UUID>> playerIP = new HashMap<>();
    Map<UUID, String> confirmationIP = new HashMap<>();

    Map<UUID, ServerInfo> playerLocations = new HashMap<>();
    Map<UUID, ServerInfo> pendingRelocations = new HashMap<>();

    boolean enableAdminMFA;
    boolean enableWhitelist;
    
    public LoginListener(JsonHandler jsonHandler, ChannelManager channelManager, TicketManager ticketManager, Set<UUID> versionCheckBypass, boolean enableAdminMFA, boolean enableWhitelist) {
        this.jsonHandler = jsonHandler;
        this.channelManager = channelManager;
        this.ticketManager = ticketManager;
        newPlayerLogins = new HashMap<>();

        this.versionCheckBypass = versionCheckBypass;
        this.enableAdminMFA = enableAdminMFA;
        this.enableWhitelist = enableWhitelist;
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
                event.setCancelReason(new TextComponent("§cYou're already logged in to this server."));
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
            int forcedProtocolVersion = 764;
            int protocolVersion = connection.getVersion();
            if(protocolVersion != forcedProtocolVersion && !versionCheckBypass.contains(uuid)) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§cPlease use §aMinecraft v1.20.2 §cfor Cubeville.\nhttp://cubeville.org/version"));
                return;
            }
        }

        if((!pdm.isPlayerKnown(uuid) || pdm.getPriority(uuid) < 61) && enableWhitelist) {
            System.out.println(connection.getName() + " blocked from logging on. Cause: Whitelist enabled");
            event.setCancelled(true);
            event.setReason(new TextComponent("§cSorry, the server is currently under maintenance. Check back shortly!"));
            return;
        }
        
        if(!pdm.isPlayerKnown(uuid)) {
            // Count number of new players in last two minutes
            int c2min = newPlayerLoginCount(120000);
            int c10min = newPlayerLoginCount(600000);
            if(c2min >= 4 || c10min >= 6 || newPlayerBlocker) {
                if(newPlayerBlocker) {
                    System.out.println(connection.getName() + " blocked from logging on. Cause: New player blocker.");
                }
                else {
                    System.out.println(connection.getName() + " blocked from logging on. Cause: Limiter (2 minutes: " + c2min + ", 10 minutes: " + c10min);
                }
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("§cSorry, all login slots are currently occupied.\n§cPlease try again in a few minutes."));
                return;
            }
        }

        if(pdm.isBanned(uuid, true)) {
            event.setCancelled(true);
            boolean perm = pdm.isPermanentlyBanned(connection.getUniqueId());
            String type = perm ? "permanently" : "temporarily";
            String endOfBan = "";
            String more = perm ? "\n§aMore Information: §eYou have been banned because a staff member has decided that you didn't play according to the Cubeville rules. If you think this is a mistake, or if you want to join the server again without violating the rules again, you can apply for an unban. For more information, visit §acubeville.org/unban§e." : "";
            if(!perm) {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss z");
                endOfBan = " until §e" + sdf.format(new Date(pdm.getEndOfTempban(uuid)));
            }
            event.setCancelReason(TextComponent.fromLegacyText("§cYou're " + type + " banned from this server" + endOfBan + ".\n§cReason: §e" + pdm.getBanReason(uuid) + more));
            return;
        }

        String ip = getStrippedIpAddress(connection);
        if(pdm.needsConfirmation(uuid, ip) && enableAdminMFA) {
            boolean ipConfirmValid = false;
            {
                File confirmation = new File("/var/www/2falogin/players/ip" + uuid.toString());
                if(confirmation.exists()) {
                    try {
                        try (BufferedReader br = new BufferedReader(new FileReader(confirmation))) {
                            String line = br.readLine();
                            if (line != null && line.equals(ip)) {
                                System.out.println("Login verification: " + ip + " equals " + line);
                                ipConfirmValid = true;
                            } else {
                                System.out.println("Login verification: " + ip + " does not equal " + line);
                            }
                        }
                    }
                    catch(Exception e) {
                        // couldn't care less
                    }
                }
            }
            if(!ipConfirmValid) {
                if(confirmationIP.get(uuid) == null || !confirmationIP.get(uuid).equals(ip)) {
                    File confirmation = new File("/var/www/2falogin/players/" + uuid);
                    long period = 1000000;
                    if(confirmation.exists()) {
                        period = System.currentTimeMillis() - confirmation.lastModified();
                    }
                    if(period > 300000) {
                        event.setCancelled(true);
                        event.setCancelReason(new TextComponent("§cPermission denied."));
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

        {
            String displayName = pdm.getPlayerDisplayName(playerId);
            if(displayName != null) player.setDisplayName(displayName);
        }
        
        boolean newPlayer = false;

        String ip = getStrippedIpAddress(player);

        if(!this.playerIPInfo.containsKey(ip)) {
            asyncQueryPlayerIPInfo(ip, player.getName());
        }

        if(!player.hasPermission("cvchat.silent.join")) {
            if(!pdm.isPlayerKnown(playerId)) {
                newPlayer = true;
                //sendWelcomeMessage(player.getName());
                pdm.addPlayer(playerId, player.getName(), ip);
                sendPublicMessage(ip, player.getDisplayName(), "joined", true);
                newPlayerLogins.put(playerId, System.currentTimeMillis());
            }
            else {
                if(pdm.getPlayerName(playerId).equals(player.getName()) || pdm.getPlayerDisplayName(playerId) != null) {
                    sendPublicMessage(ip, player.getDisplayName(), "joined", false);
                }
                else {
                    sendPublicMessage(ip, player.getDisplayName() + " (formerly known as " + pdm.getPlayerName(playerId) + ")", "joined", false);
                }
                if(!pdm.getPlayerName(playerId).equals(player.getName())) {
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
                        sendIPMatchMessage(player.getDisplayName(), names, permbanned, tempbanned);
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
                TextComponent out = new TextComponent(cnt + " open modreq(s).");
                out.setColor(ChatColor.GREEN);
                TextComponent check = new TextComponent(" /");
                check.setColor(ChatColor.GOLD);
                check.addExtra(ChatColor.GREEN + "check");
                check.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/check"));
                check.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Check ticket queue")));
                out.addExtra(check);
                out.addExtra(" for more.");
                player.sendMessage(out);
            }
        }

        if(!RankManager.getInstance().isPrefixPermitted(player, pdm.getPrefix(playerId))) {
            pdm.changePrefix(playerId, "");
        }

        List<NameRecord> nameRecords = NameRecordDao.getInstance().getNameRecords(player.getUniqueId());
        if(nameRecords.isEmpty()) {
            NameRecordDao.getInstance().addNameEntry(new NameRecord(player.getUniqueId(), player.getName()));
        } else {
            NameRecord latestRecord = null;
            for(NameRecord nR : nameRecords) {
                if(latestRecord == null) latestRecord = nR;
                if(latestRecord.getNameTime() < nR.getNameTime()) latestRecord = nR;
            }
            if(!latestRecord.getName().equals(player.getName())) {
                NameRecordDao.getInstance().addNameEntry(new NameRecord(player.getUniqueId(), player.getName()));
            }
        }

        ServerInfo attemptedServer = ProxyServer.getInstance().getReconnectHandler().getServer(player);
        ServerInfo correctServer = getCorrectServer(player);
        System.out.println(player.getName() + " is attempting to connect to " + attemptedServer.getName());
        System.out.println(player.getName() + " previously logged off on " + (correctServer == null ? "N/A" : correctServer.getName()));
        if(correctServer != null && !correctServer.equals(attemptedServer)) {
            this.pendingRelocations.put(player.getUniqueId(), correctServer);
        }

        System.out.println("Player " + player.getName() + " logged in" + (newPlayer ? " for the first time: " : ": ") + ip);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if(this.pendingRelocations.containsKey(player.getUniqueId())) {
            if(!this.pendingRelocations.get(player.getUniqueId()).equals(player.getServer().getInfo())) {
                player.connect(this.pendingRelocations.get(player.getUniqueId()));
                System.out.println(player.getName() + " is being relocated to " + this.pendingRelocations.get(player.getUniqueId()).getName());
            }
            this.pendingRelocations.remove(player.getUniqueId());
        }
        this.playerLocations.put(player.getUniqueId(), player.getServer().getInfo());
    }

    private ServerInfo getCorrectServer(ProxiedPlayer player) {
        if(this.playerLocations.containsKey(player.getUniqueId())) {
            return this.playerLocations.get(player.getUniqueId());
        } else {
            File file = new File("locations.yml");
            Yaml yaml = new Yaml();
            CaseInsensitiveMap<String> data = null;
            try {
                file.createNewFile();
                try (FileReader rd = new FileReader(file)) {
                    Map map = yaml.loadAs(rd, Map.class);
                    if (map != null) {
                        data = new CaseInsensitiveMap<>(map);
                    }
                }
            } catch (Exception ignored) {}
            if(data != null) {
                //InetSocketAddress host = player.getPendingConnection().getVirtualHost();
                String key = player.getName() + ";cubeville.org:25565";
                ServerInfo server = ProxyServer.getInstance().getServerInfo(data.get(key));
                return server;
            } else {
                return null;
            }
        }
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
            sendPublicMessage(null, player.getDisplayName(), "left", false);
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
    
    private void sendPublicMessage(String ip, String playerName, String status, boolean newPlayer) {
        boolean vpnInUse = false;
        if(ip != null && this.playerIPInfo.containsKey(ip)) {
            vpnInUse = (this.playerIPInfo.get(ip).containsKey("proxy") && this.playerIPInfo.get(ip).get("proxy").equalsIgnoreCase("true"))
                    || (this.playerIPInfo.containsKey("hosting") && this.playerIPInfo.get(ip).get("hosting").equalsIgnoreCase("true"));
        }
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(newPlayer && p.hasPermission("cvchat.informnewplayer")) {
                TextComponent out = new TextComponent("§e" + playerName + "§e " + status + " the game. §a(New player)");
                if(vpnInUse && p.hasPermission("cvchat.informvpnplayer")) {
                    TextComponent vpn = new TextComponent(ChatColor.AQUA + " (VPN)");
                    vpn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Show details")));
                    vpn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/query " + ip));
                    out.addExtra(vpn);
                }
                p.sendMessage(out);
            }
            else {
                TextComponent out = new TextComponent("§e" + playerName + "§e " + status + " the game.");
                if(vpnInUse && p.hasPermission("cvchat.informvpnplayer")) {
                    TextComponent vpn = new TextComponent(ChatColor.AQUA + " (VPN)");
                    vpn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Show details")));
                    vpn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/query " + ip));
                    out.addExtra(vpn);
                }
                p.sendMessage(out);
            }
        }
    }

    private void sendSilentMessage(String playerName, String status) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.silent.view")) {
                p.sendMessage(new TextComponent("§3" + playerName + "§3 " + status + " the game silently."));
            }
        }
    }

    private void sendWelcomeMessage(String playerName) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            p.sendMessage(new TextComponent("§eEveryone welcome Cubeville's newest player, " + playerName + "§e!"));
        }        
    }

    private void sendIPMatchMessage(String name, List<String> names, List<Boolean> permbanned, List<Boolean> tempbanned) {
        StringBuilder message = new StringBuilder();
        for(int i = 0; i < names.size(); i++) {
            if(message.length() > 0) message.append("§2, ");
            if(permbanned.get(i)) {
                message.append("§c");
            }
            else if(tempbanned.get(i)) {
                message.append("§e");
            }
            else {
                message.append("§a");
            }
            message.append(names.get(i));
        }
        message = new StringBuilder("§2" + name + " could also be: §a" + message + "§2.");
        
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.notifyipmatch")) {
                p.sendMessage(TextComponent.fromLegacyText(message.toString()));
            }
        }
    }
    private void sendVPNNotifyMessage(String ip, String player) {
        for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.informvpnplayer")) {
                TextComponent out = new TextComponent("§e" + player + "(recent login) is using a VPN." + ChatColor.AQUA + " (VPN)");
                out.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Show details")));
                out.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/query " + ip));
                p.sendMessage(out);
            }
        }
    }
    public void setNewPlayerBlocker(boolean active) {
        newPlayerBlocker = active;
    }

    public LinkedHashMap<String, String> getPlayerIPInfo(String ip) {
        if(this.playerIPInfo.containsKey(ip)) {
            return this.playerIPInfo.get(ip);
        } else {
            return new LinkedHashMap<>();
        }
    }

    public void asyncQueryPlayerIPInfo(String ip, String pName) {
        ProxyServer.getInstance().getScheduler().runAsync(CVChat.getInstance(), () -> {
            this.playerIPInfo.put(ip, jsonHandler.queryIP(ip));
            boolean vpnInUse = (this.playerIPInfo.get(ip).containsKey("proxy") && this.playerIPInfo.get(ip).get("proxy").equalsIgnoreCase("true"))
                    || (this.playerIPInfo.containsKey("hosting") && this.playerIPInfo.get(ip).get("hosting").equalsIgnoreCase("true"));
            if(vpnInUse) sendVPNNotifyMessage(ip, pName);
        });
    }
}
