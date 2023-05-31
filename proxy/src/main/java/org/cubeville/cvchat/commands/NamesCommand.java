package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.CVChat;
import org.cubeville.cvplayerdata.playerdata.NameRecord;
import org.cubeville.cvplayerdata.playerdata.NameRecordDao;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NamesCommand extends CommandBase {

    public NamesCommand() {
        super("names", "cvchat.names");
        setUsage("§c/names <player name or uuid>");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 1)) return;

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

        List<NameRecord> records = NameRecordDao.getInstance().getNameRecords(playerId);
        Collections.reverse(records);
        for(NameRecord record : records) {
            TextComponent r = new TextComponent();
            r.addExtra(ChatColor.RED + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(record.getNameTime())));
            r.addExtra(ChatColor.AQUA + " - ");
            r.addExtra(ChatColor.GOLD + record.getName());
            sender.sendMessage(r);
        }
    }
}
