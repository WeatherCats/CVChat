package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.JsonHandler;

import java.util.List;

public class QueryCommand extends CommandBase {

    JsonHandler jsonHandler;

    public QueryCommand(JsonHandler jsonHandler) {
        super("query", "cvchat.query");
        setUsage("/query <player>");
        this.jsonHandler = jsonHandler;
    }

    public void executeC(CommandSender sender, String[] args) {
        if(ProxyServer.getInstance().getPlayer(args[0]) == null || !ProxyServer.getInstance().getPlayer(args[0]).isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is not online!"));
            return;
        }
        String ip = String.valueOf(ProxyServer.getInstance().getPlayer(args[0]).getPendingConnection().getSocketAddress());
        String ipFormatted;
        try {
            ipFormatted = ip.substring(ip.indexOf("/") + 1, ip.indexOf(":"));
        } catch(IndexOutOfBoundsException ignored) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Index out of bounds error for ip formatting prior to json query, contact Toe!"));
            return;
        }
        List<String> out = jsonHandler.queryIP(ipFormatted);
        if(!out.isEmpty()) {
            sender.sendMessage(new TextComponent(ChatColor.GOLD + "IP Details for " + ipFormatted));
            for(String o : out) {
                sender.sendMessage(new TextComponent(o));
            }
        }
    }
}
