package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.playerdata.PlayerDataManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommandCheckCommand extends CommandBase {

    CVChat plugin;
    SimpleDateFormat dateFormat;

    public CommandCheckCommand(CVChat plugin) {
        super("commandcheck", "cvchat.commandcheck");
        setUsage("§c/commandcheck <player> [number of commands(up to 50)]");
        this.plugin = plugin;
        dateFormat = new SimpleDateFormat("HH:mm");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;

        UUID playerId = getPDM().getPlayerId(args[0]);
        if(playerId == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }

        if(!sender.hasPermission("cvchat.commandcheck.unlimited")) {
            if(!verifyOutranks(sender, playerId)) return;
        }

        int commands = 10;
        if(args.length > 1) {
            try {
                commands = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                sender.sendMessage("§c" + args[1] + " is not a number.");
                return;
            }
        }

        if(PlayerDataManager.getInstance().getPlayerCommands(playerId) != null) {
            List<String> entries = new ArrayList<>();
            int i = 0;
            for(Map<Long, String> messages : PlayerDataManager.getInstance().getPlayerCommands(playerId)) {
                for(Map.Entry<Long, String> message : messages.entrySet()) {
                    entries.add("(" + dateFormat.format(new Date(message.getKey())) + ")§6 - " + message.getValue());
                }
            }
            Collections.reverse(entries);
            sender.sendMessage("§dLast " + commands + " commands entered by " + args[0] + ":");
            for(String command : entries) {
                sender.sendMessage(command);
                i++;
                if(i >= commands) break;
            }
        } else {
            sender.sendMessage("§c" + args[0] + " has not entered any commands today.");
        }
    }
}
