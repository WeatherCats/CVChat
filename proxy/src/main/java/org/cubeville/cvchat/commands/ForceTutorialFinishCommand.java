package org.cubeville.cvchat.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.playerdata.ProfileEntry;
import org.cubeville.cvchat.playerdata.ProfilesDao;

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
