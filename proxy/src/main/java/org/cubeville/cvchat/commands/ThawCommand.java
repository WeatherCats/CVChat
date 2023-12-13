package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.sanctions.SanctionManager;

import java.util.UUID;

public class ThawCommand extends CommandBase{


    public ThawCommand() {
        super("thaw", "cvchat.thaw");
        setUsage("/thaw <player>");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 1)) return;
        UUID pUUID = getPDM().getPlayerId(args[0]);
        if(pUUID == null) {
            sender.sendMessage(new TextComponent("§cNo player found!"));
            return;
        }
        //if((sender instanceof ProxiedPlayer) && !verify((ProxiedPlayer) sender, !pUUID.equals(((ProxiedPlayer)sender).getUniqueId()), "§cYou can't freeze yourself, silly!")) return;
        if((sender instanceof ProxiedPlayer) && ((ProxiedPlayer)sender).getUniqueId().equals(pUUID)) {
            if(!sender.hasPermission("cvchat.sa.thaw")) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "You can't thaw yourself, silly!"));
                return;
            }
        }

        if(!SanctionManager.getInstance().isPlayerFrozen(pUUID)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is already thawed!"));
            return;
        }
        SanctionManager.getInstance().thawPlayer(pUUID);
        sender.sendMessage(new TextComponent(ChatColor.GREEN + args[0] + " is now thawed!"));
    }
}
