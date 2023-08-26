package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class TempbanCommand extends CommandBase
{
    public TempbanCommand() {
        super("tempban", "cvchat.tempban");
        setUsage("/tempban <player> <amount> <unit> <reason>");
    }

    public void executeC(CommandSender sender, String[] args) {
        String senderName = getPlayerName(sender);

        if(!verifyNotLessArguments(sender, args, 1)) return;
        
        int offset = 0;
        boolean silent = false;
        if(args[0].equals("-s")) {
            silent = true;
            offset = 1;
        }

        if(!verifyNotLessArguments(sender, args, 4 + offset)) return;

        int amount;
        try {
            amount = Integer.parseInt(args[offset + 1]);
        }
        catch(NumberFormatException e) {
            sender.sendMessage(new TextComponent("§cAmount parameter must be numeric."));
            return;
        }

        String unit = args[offset + 2];
        if("days".startsWith(unit)) {
            amount *= 86400;
        }
        else if("hours".startsWith(unit)) {
            amount *= 3600;
        }
        else if("minutes".startsWith(unit)) {
            amount *= 60;
        }
        else {
            sender.sendMessage(new TextComponent("§cUnit must be minutes, hours or days."));
            return;
        }

        if(amount > 3600 && sender.hasPermission("cvchat.tempban.limited") && !sender.hasPermission("cvchat.tempban.unlimited")) {
            sender.sendMessage(new TextComponent("§cYou can't tempban for more than 1 hour."));
            return;
        }
        
        if(amount > 604800 && !sender.hasPermission("cvchat.tempban.unlimited")) {
            sender.sendMessage(new TextComponent("§cYou can't tempban for more than 7 days."));
            return;
        }

        String banReason = joinStrings(args, offset + 3);
        UUID bannedPlayerId = getPDM().getPlayerId(args[offset]);
        if(bannedPlayerId == null) {
            sender.sendMessage(new TextComponent("§cUnknown player §e" + args[offset]));
            return;
        }
        if(!verifyOutranks(sender, bannedPlayerId)) return;
        if(getPDM().isBanned(bannedPlayerId, false) && getPDM().isPermanentlyBanned(bannedPlayerId)) {
            sender.sendMessage(new TextComponent("§e" + args[offset] + "§c is already permanently banned!"));
            return;
        }

        String bannedPlayerName = getPDM().getPlayerName(bannedPlayerId);
        SanctionManager.getInstance().banPlayer(sender, bannedPlayerId, banReason, amount, silent);

        if(!silent) {
            sendMessage(getAllPlayers(), "§e" + bannedPlayerName + "§6 was temporarily banned by §e" + senderName + "§6. Reason: §e" + banReason);
        }
        else {
            sendMessage(getAllPlayersWithPermission("cvchat.ban.notifysilent"), "§c[Silent] §e" + bannedPlayerName + "§6 was temporarily banned by §e" + senderName + "§6. Reason: §e" + banReason);
        }

        sender.sendMessage(new TextComponent("§dPlease don't forget to make a /note!"));
    }
}
