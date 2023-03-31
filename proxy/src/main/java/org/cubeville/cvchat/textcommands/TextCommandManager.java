package org.cubeville.cvchat.textcommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class TextCommandManager
{
    List<TextCommand> textCommands;
    Map<UUID, Set<Integer>> playerUsage;
    
    public TextCommandManager(Configuration config) {
        Collection<String> commands = config.getKeys();
        textCommands = new ArrayList<>();
        int id = 0;
        for(String key: commands) {
            Configuration c = (Configuration) config.get(key);
            Set<String> aliases = new HashSet<>(c.getStringList("commands"));
            boolean mandatory = c.getBoolean("mandatory");
            TextCommand tc = new TextCommand(id++, aliases, c.getStringList("text"), mandatory);
            textCommands.add(tc);
        }
        playerUsage = new HashMap<>();
    }

    public boolean executeTextCommand(CommandSender sender, String command) {
        command = command.substring(1).toLowerCase();

        for(TextCommand textCommand: textCommands) {
            if(textCommand.matches(command)) {
                for(String s: textCommand.getText()) {
                    sender.sendMessage(s);
                }
                if(sender instanceof ProxiedPlayer) {
                    UUID playerId = ((ProxiedPlayer) sender).getUniqueId();
                    if(!playerUsage.containsKey(playerId)) playerUsage.put(playerId, new HashSet<>());
                    playerUsage.get(playerId).add(textCommand.getId());
                }
                return true;
            }
        }
        return false;
    }

    public boolean mandatoryCommandsEntered(ProxiedPlayer player) {
        UUID playerId = player.getUniqueId();
        if(!playerUsage.containsKey(playerId)) return false;
        Set<Integer> c = playerUsage.get(playerId);
        for(TextCommand tc: textCommands) {
            if(tc.isMandatory()) {
                if(!c.contains(tc.getId())) return false;
            }
        }
        return true;
    }
}
