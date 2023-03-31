package org.cubeville.cvchat.commands;

import java.util.Map;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.ranks.RankManager;
import org.cubeville.cvchat.playerdata.PlayerDataManager;

public class PrefixCommand extends Command
{
    public PrefixCommand() {
        super("prefix");
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(args.length != 1) {
            if(args.length > 1) sender.sendMessage(new TextComponent("§cToo many arguments."));
            sender.sendMessage(new TextComponent("§c/prefix <list|rank>"));
            return;
        }

        Map<String, String> p = RankManager.getInstance().getPossiblePrefixes(sender);
        
        if(args[0].equals("list")) {
            StringBuilder l = new StringBuilder();
            for(String s: p.keySet()) {
                if(l.length() > 0) l.append(", ");
                l.append(s);
            }
            sender.sendMessage(new TextComponent("§e---------- §rPrefix list §e----------"));
            sender.sendMessage(new TextComponent(l.toString()));
        }

        else {
            if(p.containsKey(args[0])) {
                PlayerDataManager.getInstance().changePrefix(sender.getUniqueId(), p.get(args[0]));
                sender.sendMessage(new TextComponent("§aPrefix changed."));
            }
            else {
                sender.sendMessage(new TextComponent("§cInvalid prefix."));
            }
        }
    }
}
