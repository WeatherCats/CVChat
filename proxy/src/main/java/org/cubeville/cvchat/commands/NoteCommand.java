package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.CVChat;
import org.cubeville.cvplayerdata.playerdata.Profiles;
import org.cubeville.cvplayerdata.playerdata.ProfilesDao;

public class NoteCommand extends CommandBase
{
    CVChat plugin;

    public NoteCommand(CVChat plugin) {
        super("note", "cvchat.note");
        setUsage("§c/note <player> <comment>");
        this.plugin = plugin;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;
        
        if(!verifyNotLessArguments(sender, args, 2)) return;
        
        UUID playerId = getPDM().getPlayerId(args[0]);
        if(playerId == null) {
            sender.sendMessage(new TextComponent("§cPlayer not found."));
            return;
        }

        Profiles entry = new Profiles(System.currentTimeMillis(), joinStrings(args, 1), sender.getUniqueId());
        ProfilesDao.getInstance().addProfileEntry(playerId, entry);
        sender.sendMessage(new TextComponent("§aNote added."));
    }
}
