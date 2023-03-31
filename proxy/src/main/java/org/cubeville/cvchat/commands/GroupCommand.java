package org.cubeville.cvchat.commands;

import java.util.Collection;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;

import org.cubeville.cvchat.channels.ChannelManager;
import org.cubeville.cvchat.channels.GroupChannel;

public class GroupCommand extends CommandBase
{
    private static GroupChannel channel;

    public GroupCommand(GroupChannel channel) {
        super("group");
        this.channel = channel;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        if(args.length > 0 && "join".startsWith(args[0])) {
            if(args.length != 2) {
                player.sendMessage(new TextComponent("§cToo " + (args.length < 2 ? "few" : "many") + " arguments."));
                player.sendMessage(new TextComponent("§c/group join <player>"));
                return;
            }

            ProxiedPlayer groupPlayer = getPlayerByVisibleName(args[1]);
            if(groupPlayer == null) {
                player.sendMessage(new TextComponent("§cNo players by that name found."));
                return;
            }

            if(!channel.isInvited(player, groupPlayer)) {
                if(!player.hasPermission("cvchat.channel.group.forcejoin")) {
                    player.sendMessage(new TextComponent("§cThat player has not invited you to their group!"));
                    return;
                }
            }
            channel.leaveGroup(player);
            boolean newGroup = channel.addPlayerToGroup(groupPlayer, player);
            player.sendMessage(new TextComponent("§9[Group]§rYou have joined §6" + groupPlayer.getDisplayName() + "§r's group."));
            ChannelManager.getInstance().saveStatus(player);
            if(newGroup) ChannelManager.getInstance().saveStatus(groupPlayer);
        }
        else if(args.length > 0 && "invite".startsWith(args[0])) {
            if(args.length != 2) {
                player.sendMessage(new TextComponent("§cToo " + (args.length < 2 ? "few" : "many") + " arguments."));
                player.sendMessage(new TextComponent("§c/group invite <player>"));
                return;
            }
            ProxiedPlayer invitedPlayer = getPlayerByVisibleName(args[1]);
            if(invitedPlayer == null) {
                player.sendMessage(new TextComponent("§cNo player by that name found."));
                return;
            }
            channel.addInvitation(invitedPlayer, player);
            player.sendMessage(new TextComponent("§9[Group]§rInvitation sent to §6" + invitedPlayer.getDisplayName() + "§r."));
            invitedPlayer.sendMessage(new TextComponent("§9[Group]§6" + player.getDisplayName() + "§r has invited you to their group."));
            invitedPlayer.sendMessage(new TextComponent("Enter §b/group join " + player.getDisplayName() + "§r to accept."));
        }
        else if(args.length > 0 && "list".startsWith(args[0])) {
            if(args.length > 1) {
                player.sendMessage(new TextComponent("§cToo many arguments."));
                player.sendMessage(new TextComponent("§c/group list"));
                return;
            }
            if(!channel.isInGroup(player)) {
                player.sendMessage(new TextComponent("§cYou are not in a group."));
                return;
            }
            Collection<ProxiedPlayer> members = channel.getGroupMembers(player);
            StringBuilder list = new StringBuilder();
            for(ProxiedPlayer member: members) {
                if(!Util.playerIsUnlisted(member)) {
                    if(list.length() > 0) list.append("§r, ");
                    list.append(member.getDisplayName());
                }
            }
            player.sendMessage(new TextComponent("§9[Group]§rMembers online: " + list));
        }
        else if(args.length > 0 && "leave".startsWith(args[0])) {
            if(args.length > 1) {
                player.sendMessage(new TextComponent("§cToo many arguments."));
                player.sendMessage(new TextComponent("§c/group leave"));
                return;
            }
            if(!channel.isInGroup(player)) {
                player.sendMessage(new TextComponent("§cYou are not in a group."));
                return;
            }
            channel.leaveGroup(player);
            player.sendMessage(new TextComponent("§9[Group]§rYou have left your group."));
            ChannelManager.getInstance().saveStatus(player);
        }
        else {
            player.sendMessage(new TextComponent("§c/group <leave|invite|join|list>"));
        }
    }
}
