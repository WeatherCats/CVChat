package org.cubeville.cvchat.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.playerdata.PlayerDataManager;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class MsgCommand extends CommandBase
{
    private static Map<UUID, UUID> lastMessageReceived;
    private static Map<UUID, UUID> lastMessageSent;
    private static Map<UUID, Long> disableRefusal;
    private static final long temporaryDisableTimeout = 150000;
    
    public MsgCommand() {

        super("msg");
        setUsage("§c/msg <target> <message...>");
        lastMessageReceived = new HashMap<>();
        lastMessageSent = new HashMap<>();
        disableRefusal = new HashMap<>();
    }

    public static boolean disabledRefusal(UUID player) {
        if(!disableRefusal.keySet().contains(player)) return false;
        if(disableRefusal.get(player) == 0) return true;
        if(System.currentTimeMillis() - disableRefusal.get(player) > temporaryDisableTimeout) {
            disableRefusal.remove(player);
            return false;
        }
        else {
            return true;
        }
    }

    public static void resetTemporarilyDisabledRefusal(UUID player) {
        if(!disableRefusal.keySet().contains(player)) return;
        if(disableRefusal.get(player) == 0) return;
        if(System.currentTimeMillis() - disableRefusal.get(player) > temporaryDisableTimeout)
            disableRefusal.remove(player);
        else
            disableRefusal.put(player, System.currentTimeMillis());
    }
    
    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(sender.hasPermission("cvchat.refusepm")) {
            if(args.length == 0) {
                sender.sendMessage("§aYou " + (disabledRefusal(sender.getUniqueId()) ? "can" : "can't") + " receive private messages when you're vanished.");
                return;
            }
            else if(args.length == 1) {
                if(args[0].equals("on")) {
                    disableRefusal.put(sender.getUniqueId(), 0L);
                    sender.sendMessage("§aYou will receive private messages now.");
                    return;
                }
                else if(args[0].equals("tmp")) {
                    disableRefusal.put(sender.getUniqueId(), System.currentTimeMillis());
                    sender.sendMessage("§aYou will receive private messages temporarily now.");
                    return;
                }
                else if(args[0].equals("off")) {
                    disableRefusal.remove(sender.getUniqueId());
                    sender.sendMessage("§aYou will not receive private messages now.");
                    return;
                }
            }
        }

        if(!verifyNotLessArguments(sender, args, 2)) return;

        boolean fakeNotFound = false;
            
        ProxiedPlayer recipient = getPlayer(args[0]);
        
        if(recipient == null ||
           (Util.playerIsUnlisted(recipient) == true && recipient.hasPermission("cvchat.refusepm") == true && sender.hasPermission("cvchat.showvanished") == false && disabledRefusal(recipient.getUniqueId()) == false)) {
            sender.sendMessage("§cPlayer not found!");
            if(recipient == null) return;
            fakeNotFound = true;
        }
        
        if(SanctionManager.getInstance().isPlayerMuted(sender)) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage("§cYou are muted. You can only send messages to staff members.");
                return;
            }
        }

        if(SanctionManager.getInstance().isAllChatMuted() && !sender.hasPermission("cvchat.muteallbypass")) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage("§cAll chat is muted. You can only send messages to staff members.");
                return;
            }
        }

        long senderFirstLogin = PlayerDataManager.getInstance().getFirstLogin(sender.getUniqueId());
        if(senderFirstLogin == 0 || System.currentTimeMillis() - senderFirstLogin < 600000) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage("§cNo permission.");
                return;
            }
        }

        long recipientFirstLogin = PlayerDataManager.getInstance().getFirstLogin(recipient.getUniqueId());
        if(recipientFirstLogin == 0 || System.currentTimeMillis() - recipientFirstLogin < 600000) {
            if(!sender.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage("§cYou currently have no permission to message this player.");
                return;
            }
        }

        sendMessage(sender, recipient, args, 1, fakeNotFound);
    }

    protected static void sendMessage(ProxiedPlayer sender, ProxiedPlayer recipient, String[] args, int argsOffset, boolean fakeNotFound) {
        String message = Util.removeSectionSigns(Util.joinStrings(args, argsOffset));

        if(!fakeNotFound) sender.sendMessage("§3(To " + recipient.getDisplayName() + "§3): §r" + message);
        String mark = "";
        if(fakeNotFound) mark = "§c*";
        recipient.sendMessage("§3(From " + sender.getDisplayName() + mark + "§3): §r" + message);

        if(!fakeNotFound) {
            lastMessageSent.put(sender.getUniqueId(), recipient.getUniqueId());
            lastMessageReceived.put(sender.getUniqueId(), recipient.getUniqueId());
        }
        lastMessageReceived.put(recipient.getUniqueId(), sender.getUniqueId());
        resetTemporarilyDisabledRefusal(sender.getUniqueId());
        resetTemporarilyDisabledRefusal(recipient.getUniqueId());
    }

    protected static UUID getLastMessageReceived(UUID player) {
        return lastMessageReceived.get(player);
    }

    protected static UUID getLastMessageSent(UUID player) {
        return lastMessageSent.get(player);
    }
}
