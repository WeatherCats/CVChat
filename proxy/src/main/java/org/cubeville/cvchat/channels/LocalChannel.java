package org.cubeville.cvchat.channels;

import java.util.*;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;

public class LocalChannel extends Channel
{
    Set<UUID> localMuted;

    public LocalChannel(String name, String viewPermission, String sendPermission, String colorPermission, String leavePermission, Map<String, String> format, boolean isDefault, boolean autojoin, boolean listable, boolean filtered, Collection<String> commands) {
        super(name, viewPermission, sendPermission, colorPermission, leavePermission, format, isDefault, autojoin, listable, filtered, commands, null);
        localMuted = new HashSet<>();
    }

    protected void doSendMessage(CommandSender sender, String formattedMessage) {
        if(!(sender instanceof ProxiedPlayer)) return; // Console can't chat locally
        ProxiedPlayer player = (ProxiedPlayer) sender;

        // 1) Send to all players who monitor local chat and are on different server
        String serverName = player.getServer().getInfo().getName();
        Collection<ProxiedPlayer> allPlayers = ProxyServer.getInstance().getPlayers();
        String lm = "";
        for(ProxiedPlayer p: allPlayers) {
            if(p.hasPermission("cvchat.monitor.local")) {
                if(p.getServer().getInfo().getName().equals(serverName)) {
                    if(localMuted.contains(p.getUniqueId())) {
                        if(lm.length() > 0) lm += ",";
                        lm += p.getUniqueId().toString();
                    }
                }
            }
        }
        String idlist = player.getUniqueId().toString();
        if(lm.length() > 0) {
            idlist += ";" + lm;
        }
        // 2) Send message to player's server for further handling
        String msg = "locchat|" + idlist + "|" + formattedMessage;
        ChannelManager.getInstance().getIPC().sendMessage(serverName, msg);
    }

    public void sendMonitorMessage(String server, String formattedMessage) {
        //String finalMessage = formattedMessage;
        String iv = formattedMessage.substring(0, formattedMessage.indexOf("|"));
        Set<UUID> ivFormatted = new HashSet<>();
        //UUID playerId;
        //playerId = UUID.fromString(iv.substring(0, iv.indexOf(",")));
        StringTokenizer tk = new StringTokenizer(iv, ",");
        while(tk.hasMoreTokens()) {
            ivFormatted.add(UUID.fromString(tk.nextToken()));
        }
        String finalMessage = formattedMessage.substring(formattedMessage.indexOf("|") + 1);
        TextComponent out = new TextComponent(finalMessage);
        if(ivFormatted.size() > 0) {
            out.addExtra(" ");
            TextComponent hover = new TextComponent("§3(" + ivFormatted.size() + ")");
            String inRange = "";
            int i = ivFormatted.size();
            for(UUID p : ivFormatted) {
                inRange = inRange + ProxyServer.getInstance().getPlayer(p).getName();
                i--;
                if(i > 0) {
                    //inRange = inRange + ", ";
                    inRange = inRange + "\n";
                }
            }
            hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(inRange)));
            out.addExtra(hover);
        }
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.monitor.local")) {
                if(!p.getServer().getInfo().getName().equals(server)) {
                    if(!localMuted.contains(p.getUniqueId())) {
                        p.sendMessage(out);
                    }
                }
            }
        }
    }

    public void setLocalMonitorMute(UUID playerId, boolean mute) {
        if(mute) {
            localMuted.add(playerId);
        }
        else {
            localMuted.remove(playerId);
        }
    }
}
