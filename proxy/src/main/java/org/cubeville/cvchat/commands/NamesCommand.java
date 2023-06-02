package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvplayerdata.playerdata.NameRecord;
import org.cubeville.cvplayerdata.playerdata.NameRecordDao;

import java.text.SimpleDateFormat;
import java.util.*;

public class NamesCommand extends CommandBase {

    public NamesCommand() {
        super("names", "cvchat.names");
        setUsage("§c/names <player name or uuid> [full]");

    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 2)) return;

        UUID playerId = getPDM().getPlayerId(args[0]);
        if(playerId == null) {
            try {
                playerId = UUID.fromString(args[0]);
            } catch(IllegalArgumentException e) {
                sender.sendMessage(new TextComponent("§cPlayer not found."));
                return;
            }
            if(!getPDM().isPlayerKnown(playerId)) {
                sender.sendMessage(new TextComponent("§cPlayer not found."));
                return;
            }
        }

        boolean full = false;
        if(args.length == 2) {
            if(args[1].equalsIgnoreCase("full")) {
                full = true;
            } else {
                sender.sendMessage(new TextComponent("§c/names <player name or uuid> [full]"));
                return;
            }
        }

        List<NameRecord> records = NameRecordDao.getInstance().getNameRecords(playerId);
        Collections.sort(records);
        Collections.reverse(records);
        Iterator<NameRecord> iterator = records.iterator();
        int i = 0;
        while(iterator.hasNext()) {
            NameRecord record = iterator.next();
            TextComponent r = new TextComponent();
            if(iterator.hasNext()) {
                r.addExtra(ChatColor.RED + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(record.getNameTime())));
            } else {
                r.addExtra(ChatColor.RED + "First Known Name");
            }
            r.addExtra(ChatColor.AQUA + " - ");
            r.addExtra(ChatColor.GOLD + record.getName());
            sender.sendMessage(r);
            if(i >= 4) break;
            if(!full) i++;
        }
    }
}
