package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.playerdata.PlayerDataManager;

import java.util.*;

public class SCommand extends CommandBase {

    public SCommand() {
        super("s", "cvchat.commandsearch");
        setUsage("§c/s <text>");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;
        String searchstr = args[0];
        
        UUID playerId = sender.getUniqueId();

        if(PlayerDataManager.getInstance().getPlayerCommands(playerId) != null) {
            List<String> entries = new ArrayList<>();
            for(String message: messages.values()) {
                if(message.indexOf(searchstr) >= 0)
                    entries.add(message);
            }
            Collections.sort(entries);
            String lastentry = "";
            for(String entry: entries) {
                if(entry != lastentry)
                    sender.sendMessage("§6 - " + entry);
                lastentry = entry;
            }
        }
        else {
            sender.sendMessage("§cNo matching commands found.");            
        }

    }
}

