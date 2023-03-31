package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;

public class KickCommand extends CommandBase
{
    public KickCommand() {
        super("kick", "cvchat.kick");
        setUsage("§c/kick [-s] <player> <reason>");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;
        
        if(!verifyNotLessArguments(sender, args, 1)) return;
        
        int offset = 0;
        boolean silent = false;
        if(args[0].equals("-s")) {
            silent = true;
            offset = 1;
            if(!verifyNotLessArguments(sender, args, 2)) return;
        }

        String playerName = args[offset];
        if(!verifyOnline(sender, playerName)) return;
        ProxiedPlayer player = getPlayer(playerName);
        if(!verifyOutranks(sender, player)) return;

        String reason = Util.joinStrings(args, offset + 1);
        String s = reason.length() > 0 ? " Reason: §e" + reason : "";
        player.disconnect(new TextComponent("§6You have been kicked by §e" + sender.getDisplayName() + "§6." + s));

        if(!silent) {
            sendMessage(getAllPlayers(), "§e" + player.getDisplayName() + "§6 was kicked by §e" + sender.getDisplayName() + "§6." + s);
        }
        else {
            sendMessage(getAllPlayersWithPermission("cvchat.kick.notifysilent"), "§c[Silent] §e" + player.getDisplayName() + "§6 was kicked by §e" + sender.getDisplayName() + "§6." + s);
        }
    }
}
