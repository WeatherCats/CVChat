package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
        
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

        boolean immediate = false;
        String searchstr = "";

        if(args.length >= 2 && args[0].equals("i")) {
            immediate = true;
            searchstr = args[1];
        }
        else if(args.length >= 1) {
            searchstr = args[0];
        }

        UUID playerId = sender.getUniqueId();

        List<Map<Long, String>> playerCommands = PlayerDataManager.getInstance().getPlayerCommands(playerId);
        if(playerCommands != null) {
            List<String> entries = new ArrayList<>();
            for(Map<Long, String> messages: playerCommands) {
                for(Map.Entry<Long, String> message: messages.entrySet()) {
                    String command = message.getValue();
                    if(command.contains(searchstr) && !command.startsWith("/s ") && !command.equals("/s") && !command.startsWith("/si "))
                        entries.add(command);
                }
            }

            if(entries.size() > 0) {
                if(immediate) {
                    String cmd = entries.get(entries.size() - 1).substring(1);
                    commandSender.sendMessage(TextComponent.fromLegacyText(">> /" + cmd));
                    if(!ProxyServer.getInstance().getPluginManager().dispatchCommand(commandSender, cmd)) {
                        ProxiedPlayer player = (ProxiedPlayer) commandSender;
                        player.chat("/" + cmd);
                    }
                    return;
                }
                
                Collections.sort(entries);
                String lastentry = "";
                for(String entry: entries) {
                    if(!entry.equals(lastentry)) {
                        TextComponent chat = new TextComponent("§b[§aChat§b] ");
                        chat.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, entry));
                        TextComponent copy = new TextComponent("§b[§aClip§b] ");
                        copy.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, entry));
                        chat.addExtra(copy);
                        chat.addExtra(new TextComponent("§6" + entry));
                        sender.sendMessage(chat);
                    }
                    lastentry = entry;
                }
            }
            else {
                sender.sendMessage(new TextComponent("§cNo matching commands found."));
            }
        }
        else {
            sender.sendMessage(new TextComponent("§cNo matching commands found."));
        }

    }
}

