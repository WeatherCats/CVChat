package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class UnbanCommand extends CommandBase
{
    public UnbanCommand() {
        super("unban", "cvchat.unban");
        setUsage("/unban <player>");
    }

    public void executeC(CommandSender sender, String[] args) {
        String senderName = getPlayerName(sender);

        int offset = 0;
        boolean silent = false;
        if(args[0].equals("-s")) {
            silent = true;
            offset =1;
        }

        if(!verifyNotLessArguments(sender, args, 1 + offset)) return;

        String bannedPlayerName = SanctionManager.getInstance().unbanPlayer(sender, args[offset], silent);

        if(bannedPlayerName == null) {
            sender.sendMessage(new TextComponent("§cUnknown player §e" + args[offset]));
            return;
        }

        if(!silent) {
            sendMessage(getAllPlayers(), "§e" + bannedPlayerName + "§6 was unbanned by §e" + senderName + "§6.");
        }
        else {
            sendMessage(getAllPlayersWithPermission("cvchat.ban.notifysilent"), "§c[Silent] §e" + bannedPlayerName + "§6 was unbanned by §e" + senderName + "§6.");
        }
    }
}
