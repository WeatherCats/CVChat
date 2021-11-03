package org.cubeville.cvchat.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;

public class GroupChannel extends Channel
{
    private static Map<UUID, UUID> groupMembership; // Key = player, value = group
    private static Map<UUID, Set<UUID>> groupMembers;    // Key = group, value = player
    private static Map<UUID, UUID> invitations; // Key = invited player, value = inviting player
    
    public GroupChannel(String name, String viewPermission, String sendPermission, String colorPermission, String leavePermission, Map<String, String> format, boolean isDefault, boolean autojoin, boolean listable, boolean filtered, Collection<String> commands) {
        super(name, viewPermission, sendPermission, colorPermission, leavePermission, format, isDefault, autojoin, listable, filtered, commands, null);

        groupMembership = new HashMap<>();
        groupMembers = new HashMap<>();
        invitations = new HashMap<>();
    }

    public void playerLogin(ProxiedPlayer player, String configuration) {
        super.playerLogin(player, null);
        if(configuration != null) {
            String group = Util.getStringProperty(configuration);
            addPlayerToGroup(UUID.fromString(group), player);
        }
    }

    public String getConfigurationString(ProxiedPlayer player) {
        UUID p = player.getUniqueId();
        if(groupMembership.containsKey(p)) {
            return name + ": " + groupMembership.get(p).toString();
        }
        return null;
    }

    public void playerDisconnect(ProxiedPlayer player) {
        removeInvitations(player);
        UUID p = player.getUniqueId();
        if(groupMembership.containsKey(p)) {
            UUID group = groupMembership.get(p);
            groupMembers.get(group).remove(p);
            groupMembership.remove(p);
        }
    }

    private void addPlayerToGroup(UUID group, ProxiedPlayer player) {
        groupMembership.put(player.getUniqueId(), group);
        if(!groupMembers.containsKey(group)) groupMembers.put(group, new HashSet<>());
        groupMembers.get(group).add(player.getUniqueId());
    }
    
    public boolean addPlayerToGroup(ProxiedPlayer groupPlayer, ProxiedPlayer newMember) {
        UUID group = groupMembership.get(groupPlayer.getUniqueId());
        if(group == null) {
            group = UUID.randomUUID();
            Set<UUID> members = new HashSet<>();
            members.add(groupPlayer.getUniqueId());
            members.add(newMember.getUniqueId());
            groupMembers.put(group, members);
            groupMembership.put(groupPlayer.getUniqueId(), group);
            groupMembership.put(newMember.getUniqueId(), group);
            return true;
        }
        else {
            groupMembers.get(group).add(newMember.getUniqueId());
            groupMembership.put(newMember.getUniqueId(), group);
            return false;
        }
    }

    protected Collection<ProxiedPlayer> getRecipientList(CommandSender sender) {
        if(!(sender instanceof ProxiedPlayer)) {
            return new ArrayList<>();
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        UUID group = groupMembership.get(player.getUniqueId());
        if(group == null) return null;
        List<ProxiedPlayer> members = new ArrayList<>();
        for(UUID member: groupMembers.get(group)) {
            ProxiedPlayer memberPlayer = ProxyServer.getInstance().getPlayer(member);
            if(memberPlayer == null) {
                System.out.println("Group member " + member.toString() + " is not online.");
            }
            else {
                members.add(memberPlayer);
            }
        }
        return members;
    }

    protected void sendFailureMessage(CommandSender player) {
        player.sendMessage("§cYou're not member of a group.");
    }

    public boolean isInGroup(ProxiedPlayer player) {
        return groupMembership.containsKey(player.getUniqueId());
    }

    public void leaveGroup(ProxiedPlayer player) {
        UUID p = player.getUniqueId();
        UUID group = groupMembership.get(p);
        if(groupMembers.get(group) != null) groupMembers.get(group).remove(p);
        groupMembership.remove(p);
    }
    
    public Collection<ProxiedPlayer> getGroupMembers(ProxiedPlayer player) {
        UUID group = groupMembership.get(player.getUniqueId());
        List<ProxiedPlayer> ret = new ArrayList<>();
        for(UUID member: groupMembers.get(group)) {
            ret.add(ProxyServer.getInstance().getPlayer(member));
            // TODO: Hide vanished members!
        }
        return ret;
    }

    public void addInvitation(ProxiedPlayer invitedPlayer, ProxiedPlayer invitingPlayer) {
        invitations.put(invitedPlayer.getUniqueId(), invitingPlayer.getUniqueId());
    }

    public boolean isInvited(ProxiedPlayer joiningPlayer, ProxiedPlayer groupPlayer) {
        return invitations.get(joiningPlayer.getUniqueId()) != null && invitations.get(joiningPlayer.getUniqueId()).equals(groupPlayer.getUniqueId());
    }

    // TODO: Remove invitations if a player switches or leaves a group or logs off
    public void removeInvitations(ProxiedPlayer player) {
    }

}
