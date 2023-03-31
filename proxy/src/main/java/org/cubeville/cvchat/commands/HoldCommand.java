package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import org.cubeville.cvchat.tickets.TicketManager;

public class HoldCommand extends CommandBase
{
    TicketManager ticketManager;

    public HoldCommand(TicketManager ticketManager) {
        super("hold", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length != 1) {
            commandSender.sendMessage("§cWrong number of arguments.");
            commandSender.sendMessage("§c/hold <modreq-id>");
            return;
        }

        try {
            int id = Integer.parseInt(args[0]);
            ticketManager.holdTicket(commandSender, id);
        }
        catch (NumberFormatException e) {
            commandSender.sendMessage("§cInvalid ticket id.");
        }
    }
}
