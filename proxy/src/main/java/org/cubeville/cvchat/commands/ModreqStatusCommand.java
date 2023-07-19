package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.tickets.Ticket;
import org.cubeville.cvchat.tickets.TicketManager;
import org.cubeville.cvipc.CVIPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ModreqStatusCommand extends CommandBase {

    CVChat plugin;
    TicketManager ticketManager;
    CVIPC cvipc;

    public ModreqStatusCommand(CVChat plugin, TicketManager ticketManager, CVIPC cvipc) {
        super("modreqstatus", "cvchat.modreqstatus");
        setUsage("§c/modreqstatus");
        this.plugin = plugin;
        this.ticketManager = ticketManager;
        this.cvipc = cvipc;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        if(args.length > 1) return;
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        String server = player.getServer().getInfo().getName();
        UUID puuid = getPDM().getPlayerId(player.getName());
        List<Ticket> tickets = ticketManager.getTicketsByPlayer(puuid);
        Collections.reverse(tickets);
        int page = 1;
        int totalPages = 0;
        if(args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
                if(page < 1) return;
            } catch(NumberFormatException ignored) {
                return;
            }
        }
        int totalTickets = tickets.size();
        int size = 54;
        /*if(totalTickets <= 9) {
            size = 18;
        } else if(totalTickets <= 18) {
            size = 27;
        } else if(totalTickets <= 27) {
            size = 36;
        } else if(totalTickets <= 36) {
            size = 45;
        } else {
            size = 54;
        }*/
        while(totalTickets > 0) {
            totalTickets = totalTickets - 45;
            totalPages++;
        }
        if(page > totalPages) return;
        for(int i = 1; i <= (45 * (page - 1)); i++) {
            tickets.remove(tickets.get(0));
        }


        cvipc.sendMessage(server, "cmd|console|menu createtemp Modreqs player:" + player.getName() + " size:" + size);

        int slot = 0;
        for(Ticket ticket : tickets) {
            String item;
            String itemName = ChatColor.GOLD + "#" + ticket.getId() + " Status: " + ChatColor.AQUA;
            List<String> playerMsgList = new ArrayList<>();
            String playerMsg = ticket.getText() == null ? ticket.getText() : ticket.getText().replace("\"", "'");
            List<String> modMsgList = new ArrayList<>();
            String modMsg = ticket.getModeratorText() == null ? ticket.getModeratorText() : ticket.getModeratorText().replace("\"", "'");
            if(playerMsg != null) {
                while(playerMsg.length() > 50) {
                    playerMsgList.add(playerMsg.substring(0, 50));
                    playerMsg = playerMsg.substring(50);
                }
                playerMsgList.add(playerMsg);
            } else {
                playerMsgList.add("[none]");
            }
            if(modMsg != null) {
                while(modMsg.length() > 50) {
                    modMsgList.add(modMsg.substring(0, 50));
                    modMsg = modMsg.substring(50);
                }
                modMsgList.add(modMsg);
            } else {
                modMsgList.add("[none]");
            }
            StringBuilder lore = new StringBuilder();
            lore.append(ChatColor.GOLD + "Your Modreq: ");
            for(String p : playerMsgList) {
                lore.append(ChatColor.AQUA + p + ";");
            }
            lore.append(ChatColor.GOLD + "Moderator Response: ");
            for(String m : modMsgList) {
                lore.append(ChatColor.AQUA + m + ";");
            }
            boolean enchant = false;
            if(ticket.isClosed()) {
                item = "book";
                itemName = itemName.concat("Closed by " + ticket.getModeratorName());
                enchant = true;
            } else if(ticket.isClaimed()) {
                item = "writable_book";
                itemName = itemName.concat("In-Progress by " + ticket.getModeratorName());
            } else if(ticket.isHeld()){
                item = "book";
                itemName = itemName.concat("Held");
            } else {
                item = "book";
                itemName = itemName.concat("Open");
            }
            setItems(player, server, enchant, slot, item, itemName, lore.toString());
            slot++;
            if(slot > 44) break;
        }
        int i = setRest(slot, server, player, totalPages, page);
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu displaytemp Modreqs player:" + commandSender.getName());
        }, i, TimeUnit.MILLISECONDS);
    }

    public void setItems(ProxiedPlayer player, String server, boolean enchant, int slot, String item, String itemName, String lore) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs player:" + player.getName() + (enchant ? " itemenchantment:durability" : "")
                    + " slot:" + slot + " item:" + item + " itemname:\"" + itemName + "\" lore:\"" + lore + "\"");
        }, slot + 2, TimeUnit.MILLISECONDS);
    }

    public Integer setRest(int slot, String server, ProxiedPlayer player, int totalPages, int page) {
        int i = slot + 2;
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs player:" + player.getName() + " slot:53" + " item:barrier" + " itemname:" + ChatColor.GOLD + "Exit");
        }, i, TimeUnit.MILLISECONDS);
        i = i + 2;
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:53");
        }, i, TimeUnit.MILLISECONDS);
        i = i + 2;
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs player:" + player.getName() + " slot:45" + " item:feather" + " itemname:" + "\"" + ChatColor.GOLD + "Use /modreq to submit a new one\"");
        }, i, TimeUnit.MILLISECONDS);
        i = i + 2;
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:45");
        }, i, TimeUnit.MILLISECONDS);
        i = i + 2;


        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs player:" + player.getName() + " slot:46" + (page == 1 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 1\"");
        }, i, TimeUnit.MILLISECONDS);
        i = i + 2;
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:46");
        }, i, TimeUnit.MILLISECONDS);
        i = i + 2;
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:46" + " command:\"pcmd su " + player.getName() + " modreqstatus 1\"");
        }, i, TimeUnit.MILLISECONDS);
        if(totalPages >= 2) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs" + " size:2" + " player:" + player.getName() + " slot:47" + (page == 2 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 2\"");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:47");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:47" + " command:\"pcmd su " + player.getName() + " modreqstatus 2\"");
            }, i, TimeUnit.MILLISECONDS);
        }
        if(totalPages >= 3) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs" + " size:3" + " player:" + player.getName() + " slot:48" + (page == 3 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 3\"");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:48");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:48" + " command:\"pcmd su " + player.getName() + " modreqstatus 3\"");
            }, i, TimeUnit.MILLISECONDS);
        }
        if(totalPages >= 4) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs" + " size:4" + " player:" + player.getName() + " slot:49" + (page == 4 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 4\"");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:49");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:49" + " command:\"pcmd su " + player.getName() + " modreqstatus 4\"");
            }, i, TimeUnit.MILLISECONDS);
        }
        if(totalPages >= 5) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs" + " size:5" + " player:" + player.getName() + " slot:50" + (page == 5 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 5\"");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:50");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:50" + " command:\"pcmd su " + player.getName() + " modreqstatus 5\"");
            }, i, TimeUnit.MILLISECONDS);
        }
        if(totalPages >= 6) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs" + " size:6" + " player:" + player.getName() + " slot:51" + (page == 6 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 6\"");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:51");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:51" + " command:\"pcmd su " + player.getName() + " modreqstatus 6\"");
            }, i, TimeUnit.MILLISECONDS);
        }
        if(totalPages >= 7) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setitemtemp Modreqs" + " size:7" + " player:" + player.getName() + " slot:52" + (page == 7 ? " item:map" : " item:paper") + " itemname:" + "\"" + ChatColor.GOLD + "Page 7\"");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu setclosetemp Modreqs true player:" + player.getName() + " slot:52");
            }, i, TimeUnit.MILLISECONDS);
            i = i + 2;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                cvipc.sendMessage(server, "cmd|console|menu addcommandtemp Modreqs player:" + player.getName() + " slot:52" + " command:\"pcmd su " + player.getName() + " modreqstatus 7\"");
            }, i, TimeUnit.MILLISECONDS);
        }

        return i;
    }
}
