package org.cubeville.cvchat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class AutoMessager
{
    private final List<String> messages;
    private int next;
    
    public AutoMessager(int delay, List<String> messages, Plugin plugin) {
        this.messages = messages;
        next = 0;
        if(messages == null) return;
        ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
                public void run() {
                    String msg = messages.get(next);
                    for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
                        if(!p.hasPermission("cvchat.disableautomessages")) {
                            p.sendMessage(msg);
                        }
                    }
                    next += 1;
                    next %= messages.size();
                }
            }, delay, delay, TimeUnit.SECONDS);
    }
}
