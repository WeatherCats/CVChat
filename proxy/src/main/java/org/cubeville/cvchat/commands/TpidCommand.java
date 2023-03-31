package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import org.cubeville.cvchat.tickets.TicketManager;

public class TpidCommand extends CommandBase
{
    TicketManager ticketManager;

    public TpidCommand(TicketManager ticketManager) {
        super("tpid", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length != 1) {
            commandSender.sendMessage("/tpid <modreq-id>");
            return;
        }
        ticketManager.tpid(commandSender, Integer.parseInt(args[0]));
    }
}
