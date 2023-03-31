package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvipc.CVIPC;

import org.cubeville.cvchat.playerdata.PlayerDataManager;
import org.cubeville.cvchat.textcommands.TextCommandManager;

public class FinishCommand extends Command
{
    CVIPC ipc;
    TextCommandManager textCommandManager;
    
    public FinishCommand(CVIPC ipc, TextCommandManager textCommandManager) {
        super("finish");
        this.ipc = ipc;
        this.textCommandManager = textCommandManager;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;
        
        if(args.length > 0) {
            sender.sendMessage(new TextComponent("§cToo many arguments."));
            return;
        }

        PlayerDataManager pdm = PlayerDataManager.getInstance();

        if(pdm.finishedTutorial(sender.getUniqueId())) {
            sender.sendMessage(new TextComponent("§cYou already finished the tutorial. Enter /hub to teleport to the spawn hub."));
            return;
        }

        if(!textCommandManager.mandatoryCommandsEntered(sender)) {
            sender.sendMessage(new TextComponent("§cBefore finishing the tutorial, you have to enter the list of commands on the signs, one by one, and read each response carefully. Start with /help, then next enter /help 2 and so on."));
            return;
        }
        
        pdm.setFinishedTutorial(sender.getUniqueId());
        
        String tpCmd = "xwportal|" + sender.getUniqueId() + "|portal:TutorialFinish|survival";
        ipc.sendMessage("survival", tpCmd);
    }
}
