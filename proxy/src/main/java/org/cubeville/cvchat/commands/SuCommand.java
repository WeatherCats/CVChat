package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SuCommand extends CommandBase
{
    public SuCommand() {
        super("su", "cvchat.su");
        setUsage("§c/su <player> <command> <args>...");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!verifyNotLessArguments(commandSender, args, 2)) return;

        ProxyServer server = ProxyServer.getInstance();

        CommandSender tplayer;
        if(args[0].equalsIgnoreCase("console")) {
            tplayer = server.getConsole();
        }
        else {
            if(!verifyOnline(commandSender, args[0])) return;
            tplayer = server.getPlayer(args[0]);
        }

        String cmd = joinStrings(args, 1);
        if(cmd.startsWith("/")) cmd = cmd.substring(1);
        commandSender.sendMessage(new TextComponent("§aRun as §e" + tplayer.getName() + "§a: " + "/" + cmd));
        if(!server.getPluginManager().dispatchCommand(tplayer, cmd)) {
            if(tplayer instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) tplayer;
                player.chat("/" + cmd);
            }
            else {
                commandSender.sendMessage(new TextComponent("§cCan't run command as console."));
            }
        }
    }
}
