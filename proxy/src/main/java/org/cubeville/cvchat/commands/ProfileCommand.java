package org.cubeville.cvchat.commands;

import java.text.SimpleDateFormat;
import java.util.*;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import net.md_5.bungee.api.chat.hover.content.Text;
import org.checkerframework.checker.units.qual.C;
import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.sanctions.SanctionManager;
import org.cubeville.cvplayerdata.playerdata.*;

public class ProfileCommand extends CommandBase
{
    CVChat plugin;
    SimpleDateFormat dateFormat;
    
    public ProfileCommand(CVChat plugin) {
        super("profile", "cvchat.profile");
        setUsage("§c/profile <player> [full]"); 
        this.plugin = plugin;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(!verifyNotLessArguments(sender, args, 1)) return;
        if(!verifyNotMoreArguments(sender, args, 4)) return;

        boolean showFullProfile = false;
        List<String> searchTerms = new ArrayList<>();
        for (String arg : args) {
            if (arg.equals("full")) {
                showFullProfile = true;
            } else {
                searchTerms.add(arg);
            }
        }

        UUID playerId = null;
        if(searchTerms.size() == 1) {
            playerId = PlayerDataManager.getInstance().getPlayerId(searchTerms.get(0));
        }

        if(playerId == null) {
            List<String> searchNames = getPDM().getMatchingPlayerNames(searchTerms);
            if(searchNames.size() == 0 || args[0].length() < 4) {
                sender.sendMessage(new TextComponent("§cPlayer not found."));
                return;
            }
            else if(searchNames.size() == 1) {
                playerId = getPDM().getPlayerId(searchNames.get(0));
            }
            else {
                ComponentBuilder n = new ComponentBuilder();
                n.append("Player not found. Did you mean ").color(ChatColor.RED);
                int cnt = 0;
                for(String name: searchNames) {
                    if(cnt > 0) {
                        n.append(", ").color(ChatColor.WHITE);
                        cnt += 2;
                    }
                    if(cnt >= 150) {
                        n.append("...").color(ChatColor.WHITE);
                        break;
                    }
                    TextComponent nn = new TextComponent(name);
                    nn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile " + name));
                    n.append(nn).color(ChatColor.GREEN);
                    cnt += name.length();
                }
                n.append("?").color(ChatColor.WHITE);
                sender.sendMessage(n.create());
                return;
            }
        }
        if(!sender.hasPermission("cvchat.profile.unlimited")) {
            if(!verifyOutranks(sender, playerId)) return;
        }

        String playerName = getPDM().getPlayerName(playerId);
        boolean isOnline = ProxyServer.getInstance().getPlayer(playerId) != null;
        boolean finishedTutorial = getPDM().finishedTutorial(playerId);

        TextComponent name = new TextComponent("§4* §r" + playerName);
        TextComponent plus = new TextComponent(" §a(+)");
        List<NameRecord> records = NameRecordDao.getInstance().getNameRecords(playerId);
        Collections.sort(records);
        Collections.reverse(records);
        plus.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Previous name: " + (records.size() > 1 ? "§6" + records.get(1).getName() + "\n§fClick for more" : "§cunknown"))));
        plus.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/names " + playerName));
        name.addExtra(plus);
        sender.sendMessage(name);

        if(!finishedTutorial) {
            sender.sendMessage(new TextComponent("§4! §cPlayer has not finished the tutorial"));
        }

        if(SanctionManager.getInstance().getFilteredMessages(playerId) != null) {
            TextComponent swearKicks = new TextComponent("§cToday's Swear Kicks: §6" + SanctionManager.getInstance().getFilteredMessages(playerId).size());
            if(sender.hasPermission("cvchat.swearcheck")) {
                swearKicks.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Check swear history.")));
                swearKicks.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/swearcheck " + playerName));
            }
            sender.sendMessage(swearKicks);
        }
        
        if(sender.hasPermission("cvchat.profile.extended")) {
            sender.sendMessage(new TextComponent("§4- §r" + (isOnline ? "Peer" : "Last") + " address: §9" + getPDM().getIPAddress(playerId)));
        }

        String lastOnline;
        if(isOnline) {
            lastOnline = "§aOnline now";
        }
        else {
            lastOnline = "§9";
            long lastOnlineTime = getPDM().getLastOnline(playerId);
            long diff = (System.currentTimeMillis() - lastOnlineTime) / 1000;
            if(diff < 240) {
                lastOnline ="just now";
            }
            else if(diff < 7200) {
                lastOnline += diff / 60 + " minutes ago";
            }
            else if(diff < 172800) {
                lastOnline += diff / 3600 + " hours ago";
            }
            else if(diff < 1209600) {
                lastOnline += diff / 86400 + " days ago";
            }
            else if(diff < 5356800) {
                lastOnline += diff / 604800 + " weeks ago";
            }
            else {
                lastOnline += diff / 2592000 + " months ago";
            }
            lastOnline += "§r (" + dateFormat.format(new Date(lastOnlineTime)) + ")";
        }
        final String lastOnlineMessage = "§4- §rLast on: " + lastOnline;
        String firstLogin;
        {
            long firstLoginTime = getPDM().getFirstLogin(playerId);
            firstLogin = dateFormat.format(new Date(firstLoginTime));
        }
        final String firstLoginMessage = "§4- §rFirst login: " + firstLogin;
        
        // TODO: current location
        final UUID fPlayerId = playerId;
        boolean fShowFullProfile = showFullProfile;
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            List<Profiles> entries = ProfilesDao.getInstance().getProfileEntries(fPlayerId);
            int cnt = 0;
            int more = 0;
            for(Profiles entry: entries) {
                cnt++;
                if(cnt <= 4 || fShowFullProfile) {
                    String txt = "§c" + dateFormat.format(new Date(entry.getCommentTime())) + "§r " + entry.getComment() + " [" + getPDM().getPlayerName(entry.getAuthor()) + "]";
                    sender.sendMessage(new TextComponent(txt));
                }
                else
                    more++;
            }
            if(more > 0) {
                sender.sendMessage(new TextComponent("§c...and " + more + " more, to view them enter /profile <player> full"));
            }
            sender.sendMessage(new TextComponent(firstLoginMessage));
            sender.sendMessage(new TextComponent(lastOnlineMessage));
        });
    }
}
