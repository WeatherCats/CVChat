package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.LoginListener;

import java.util.LinkedHashMap;

public class QueryCommand extends CommandBase {

    LoginListener loginListener;

    public QueryCommand(LoginListener loginListener) {
        super("query", "cvchat.query");
        setUsage("/query <player>");
        this.loginListener = loginListener;
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 1)) return;

        String ipFormatted;
        if(args[0].contains(".")) {
            ipFormatted = args[0];
        } else if(ProxyServer.getInstance().getPlayer(args[0]) == null || !ProxyServer.getInstance().getPlayer(args[0]).isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is not online!"));
            return;
        } else {
            String ip = ProxyServer.getInstance().getPlayer(args[0]).getPendingConnection().getAddress().toString();
            ipFormatted = ip.substring(ip.indexOf("/") + 1, ip.indexOf(":"));
        }

        LinkedHashMap<String, String> out = loginListener.getPlayerIPInfo(ipFormatted);
        if(!out.isEmpty()) {
            sender.sendMessage(new TextComponent(ChatColor.GOLD + "IP details for " + ipFormatted));
            for(String key : out.keySet()) {
                String b = out.get(key);
                if(b.equalsIgnoreCase("true")) {
                    b = ChatColor.GREEN + b;
                } else if(b.equalsIgnoreCase("false")) {
                    b = ChatColor.RED + b;
                } else {
                    b = ChatColor.YELLOW + b;
                }
                sender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + key + ": " + b));
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "No IP details for " + ipFormatted + " This is probably an error!"));
        }
    }
}
