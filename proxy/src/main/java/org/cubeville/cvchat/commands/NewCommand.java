package org.cubeville.cvchat.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.playerdata.PlayerDataManager;

public class NewCommand extends CommandBase
{
    public NewCommand() {
        super("new", "cvchat.shownewplayers");
        setUsage("§c/new [minutes]");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotMoreArguments(sender, args, 1)) return;

        int minutes = 15;
        if(args.length == 1) {
            minutes = Integer.parseInt(args[0]);
        }

        long milliseconds = minutes * 60000L;
        
        PlayerDataManager pdm = PlayerDataManager.getInstance();
        long now = System.currentTimeMillis();

        List<String> newPlayers = new ArrayList<>();

        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()) {
            if(pdm.getFirstLogin(player.getUniqueId()) >= now - milliseconds) {
                newPlayers.add(player.getDisplayName());
            }
        }
        newPlayers.sort(Comparator.comparing(String::toString));

        if(newPlayers.size() == 0) {
            sender.sendMessage("§cNo new players joined in the last " + minutes + " minutes.");
        }
        else {
            sender.sendMessage("§aJoined in the last " + minutes + " minutes: §e" + String.join(", ", newPlayers));
        }
    }
}
