package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.channels.ChannelManager;

public class ChannelCommand extends Command
{
    ChannelManager channelManager;
    
    public ChannelCommand(ChannelManager channelManager) {
        super("ch", null, "cha", "chan", "chann", "channe", "channel");
        this.channelManager = channelManager;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        if(args.length > 0 && ("join".startsWith(args[0]) || "leave".startsWith(args[0]))) {
            if(args.length != 2) {
                if(args.length == 1) player.sendMessage("§cToo few arguments.");
                else player.sendMessage("§cToo many arguments.");
                player.sendMessage("§c/channel " + args[0] + " <name>");
                return;
            }

            String channelName = args[1].toLowerCase();
            if(!channelManager.getChannelMap().containsKey(channelName)) {
                player.sendMessage("§cThat channel doesn't exist.");
                return;
            }
            boolean changed = false;
            if (args[0].charAt(0) == 'l') {
                changed = channelManager.getChannelMap().get(channelName).leave(player);
            }
            else {
                changed = channelManager.getChannelMap().get(channelName).join(player);
            }
            if (changed) channelManager.saveStatus(player);
        }
        else if(args.length > 0 && "list".startsWith(args[0])) {
            if(args.length != 1) {
                player.sendMessage("§cToo many arguments.");
                player.sendMessage("§c/channel list");
                return;
            }
            StringBuilder channelList = new StringBuilder();
            for(String channel: channelManager.getChannelMap().keySet()) {
                if(channelManager.getChannelMap().get(channel).canList(player)) {
                    if(channelList.length() > 0) channelList.append("§a,§r ");
                    channelList.append(channel);
                }
            }
            player.sendMessage("§e----- §6Channel List§e -----------");
            player.sendMessage(channelList.toString());
        }
        else {
            player.sendMessage("§c/channel <leave|join|list>");
        }
    }
}
