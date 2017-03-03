package org.cubeville.cvchat;

import java.util.Collection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.cubeville.cvchat.channels.Channel;
import org.cubeville.cvchat.channels.ChannelManager;

public class LoginListener implements Listener
{
    ChannelManager channelManager;
    
    public LoginListener(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        channelManager.playerLogin(player);
        if(!player.hasPermission("cvchat.silentjoin")) {
            sendMessage(player.getDisplayName(), "joined");
        }
        else {
            System.out.println(player.getDisplayName() + " joining silently.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDisconnect(final PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        channelManager.playerDisconnect(player);
        if(!player.hasPermission("cvchat.silentleave")) {
            sendMessage(player.getDisplayName(), "left");
        }
        else {
            System.out.println(player.getDisplayName() + " disconnecting silently.");
        }
    }

    private void sendMessage(String playerName, String status) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            p.sendMessage("§e" + playerName + "§e " + status + " the game. (cvchat)");
        }
    }
}
