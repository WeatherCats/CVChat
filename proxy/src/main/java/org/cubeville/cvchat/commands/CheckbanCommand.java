package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class CheckbanCommand extends CommandBase
{
    public CheckbanCommand() {
        super("checkban");
        setUsage("/checkban <player>");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 1)) return;

        String playerName = args[0];
        UUID playerId = getPDM().getPlayerId(playerName);
        if(playerId == null) {
            sender.sendMessage(new TextComponent("§cUnknown player §e" + args[0]));
            return;            
        }
        playerName = getPDM().getPlayerName(playerId);

        if(getPDM().isBanned(playerId, false)) {
            if(getPDM().isPermanentlyBanned(playerId)) {
                sender.sendMessage(new TextComponent("§c" + playerName + "§c is permanently banned."));
            }
            else {
                sender.sendMessage(new TextComponent("§c" + playerName + "§c is temporarily banned."));
            }
            sender.sendMessage(new TextComponent("§cReason: §r" + getPDM().getBanReason(playerId)));
        }
        else {
            sender.sendMessage(new TextComponent("§a" + playerName + "§a is not banned."));
        }
    }
}
