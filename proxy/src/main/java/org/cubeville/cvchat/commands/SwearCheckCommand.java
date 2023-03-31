package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.sanctions.SanctionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class SwearCheckCommand extends CommandBase {

    CVChat plugin;
    SimpleDateFormat dateFormat;

    public SwearCheckCommand(CVChat plugin) {
        super("swearcheck", "cvchat.swearcheck");
        setUsage("§c/swearcheck <player>");
        this.plugin = plugin;
        dateFormat = new SimpleDateFormat("HH:mm");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;

        UUID playerId = getPDM().getPlayerId(args[0]);
        if(playerId == null) {
            sender.sendMessage(new TextComponent("§cPlayer not found."));
            return;
        }

        if(SanctionManager.getInstance().getFilteredMessages(playerId) != null) {
            sender.sendMessage(new TextComponent("§cToday's Swear Kicks for " + args[0] + ":"));
            for(Map<Long, String> messages : SanctionManager.getInstance().getFilteredMessages(playerId)) {
                for(Map.Entry<Long, String> message : messages.entrySet()) {
                    sender.sendMessage(new TextComponent("(" + dateFormat.format(new Date(message.getKey())) + ")§6 - " + message.getValue()));
                }
            }
        } else {
            sender.sendMessage(new TextComponent("§cWoo! " + args[0] + " has not been swear kicked today."));
        }
    }
}