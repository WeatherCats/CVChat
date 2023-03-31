package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class BanCommand extends CommandBase
{
    public BanCommand() {
        super("ban", "cvchat.ban");
        setUsage("/ban <player> <reason>");
    }

    public void executeC(CommandSender sender, String[] args) {
        String senderName = getPlayerName(sender);
        
        int offset = 0;
        boolean silent = false;
        if(args[0].equals("-s")) {
            silent = true;
            offset = 1;
        }
        
        if(!verifyNotLessArguments(sender, args, 2 + offset)) return;

        String banReason = joinStrings(args, offset + 1);
        UUID bannedPlayerId = getPDM().getPlayerId(args[offset]);
        if(bannedPlayerId == null) {
            sender.sendMessage(new TextComponent("§cUnknown player §e" + args[offset]));
            return;
        }
        if(!verifyOutranks(sender, bannedPlayerId)) return;
        
        String bannedPlayerName = getPDM().getPlayerName(bannedPlayerId);
        if(SanctionManager.getInstance().banPlayer(sender, bannedPlayerId, banReason, 0, silent)) {
            if(!silent) {
                sendMessage(getAllPlayers(), "§e" + bannedPlayerName + "§6 was banned by §e" + senderName + "§6. Reason: §e" + banReason);
            }
            else {
                sendMessage(getAllPlayersWithPermission("cvchat.ban.notifysilent"), "§c[Silent] §e" + bannedPlayerName + "§6 was banned by §e" + senderName + "§6. Reason: §e" + banReason);
            }

            sender.sendMessage(new TextComponent("§dPlease don't forget to make a /note!"));
        }
        else {
            sender.sendMessage(new TextComponent("§cPlayer is already banned permanently."));
        }
    }
}
