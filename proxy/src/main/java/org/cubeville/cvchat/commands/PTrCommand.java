package org.cubeville.cvchat.commands;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;

import org.cubeville.cvchat.Util;

public class PTrCommand extends CommandBase
{
    public PTrCommand() {
        super("ptr", "cvchat.ptr");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length < 2) {
            commandSender.sendMessage("§c/ptr [!]<player>[,player...] <message>");
            return;
        }

        String originalMessage = joinStrings(args, 1);
        String message = Util.translateAlternateColorCodes(originalMessage);

        String a0 = args[0];
        
        boolean invertPlayerList = false;
        if(a0.charAt(0) == '!') {
            a0 = a0.substring(1);
            invertPlayerList = true;
        }

        String[] playerNames = a0.split(",");

        StringBuilder notFound = new StringBuilder();

        Set<UUID> listedPlayers = new HashSet<>();

        for (String playerName : playerNames) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
            if (player == null) {
                if (notFound.length() > 0) notFound.append(", ");
                notFound.append(playerName);
            } else {
                listedPlayers.add(player.getUniqueId());
            }
        }

        if(notFound.length() > 0) {
            if(commandSender instanceof ProxiedPlayer) {
                commandSender.sendMessage("§cPlayers not found: " + notFound + ", not sending any messages.");
                return;
            }
        }
        
        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()) {
            if(listedPlayers.contains(player.getUniqueId()) ^ invertPlayerList)
                sendMessage(player, message);
        }
        
        if(commandSender instanceof ProxiedPlayer)
            commandSender.sendMessage("§8ptr: §r" + message);
    }
}
