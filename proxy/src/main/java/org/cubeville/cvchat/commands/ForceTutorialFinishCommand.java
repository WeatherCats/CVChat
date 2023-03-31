package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;

public class ForceTutorialFinishCommand extends CommandBase
{
    public ForceTutorialFinishCommand() {
        super("forcetutfinish", "cvchat.forcetutfinish");
        setUsage("§c/forcetutfinish <player>");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 1)) return;

        UUID playerId = getPDM().getPlayerId(args[0]);
        if(playerId == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }
        
        getPDM().setFinishedTutorial(playerId);

        sender.sendMessage("§aPlayer's tutorial status set to finished.");
    }
}
