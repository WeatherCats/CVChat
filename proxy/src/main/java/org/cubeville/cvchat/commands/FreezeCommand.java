package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.sanctions.SanctionManager;

import java.util.UUID;

public class FreezeCommand extends CommandBase {

    public FreezeCommand() {
        super("freeze", "cvchat.freeze");
        setUsage("/freeze <player> [range] [allowmovement]");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 2)) return;
        UUID pUUID = getPDM().getPlayerId(args[0]);
        if(pUUID == null) {
            sender.sendMessage(new TextComponent("§cNo player found!"));
            return;
        }
        if((sender instanceof ProxiedPlayer) && !verify((ProxiedPlayer) sender, !pUUID.equals(((ProxiedPlayer)sender).getUniqueId()), "§cYou can't freeze yourself, silly!")) return;

        int range = 0;
        if(args.length > 1) {
            if(args[1].equalsIgnoreCase("allowmovement")) {
                range = -1;
            } else {
                try {
                    range = Integer.parseInt(args[1]);
                    if(range < 0) range = 0;
                } catch(NumberFormatException ignored) { }
            }
        }

        if(SanctionManager.getInstance().isPlayerFrozen(pUUID)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is already frozen!"));
            return;
        }
        SanctionManager.getInstance().freezePlayer(pUUID, range);
        sender.sendMessage(new TextComponent(ChatColor.GREEN + args[0] + " is now frozen!"));
    }
}
