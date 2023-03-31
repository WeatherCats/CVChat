package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.LoginListener;

public class BlockNewPlayersCommand extends Command
{
    LoginListener loginListener;
    
    public BlockNewPlayersCommand(LoginListener loginListener) {
        super("blocknewplayers", "cvchat.blocknewplayers");
        this.loginListener = loginListener;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;

        boolean bnp = false;
        boolean valid = true;
        
        if(args.length == 0) {
            bnp = true;
        }
        else if(args.length == 1) {
            if(args[0].equals("on")) {
                bnp = true;
            }
            else if(!args[0].equals("off")) {
                valid = false;
            }
        }
        else {
            valid = false;
        }

        if(!valid) {
            commandSender.sendMessage("§c/blocknewplayers <on|off>");
        }
        else {
            loginListener.setNewPlayerBlocker(bnp);
            commandSender.sendMessage("§aNew player blocker is now " + (bnp ? "on" : "off") + ".");
        }
    }
    
}
