package org.cubeville.cvchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import org.cubeville.cvvanish.CVVanish;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

import org.cubeville.commons.utils.BlockUtils;

public class CVChat extends JavaPlugin implements Listener, IPCInterface
{
    CVIPC ipc;
    CVVanish cvvanish;

    Set<String> localChatRegions;

    int localChatDistance;
    
    Map<UUID, Long> lastGlobalChatInfo = new HashMap<>();
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);

        ipc = (CVIPC) pm.getPlugin("CVIPC");
        ipc.registerInterface("locchat", this);
        ipc.registerInterface("chatquery", this);

        cvvanish = (CVVanish) Bukkit.getServer().getPluginManager().getPlugin("CVVanish");

        if(getConfig().getStringList("localchatregions") == null) {
            System.out.println("No local chat regions found.");
            localChatRegions = new HashSet<>();
        }
        else {
            System.out.println("Reading local chat region list from config.");
            localChatRegions = new HashSet<>(getConfig().getStringList("localchatregions"));
        }

        localChatDistance = getConfig().getInt("localchatdistance", 55);
        System.out.println("Local chat distance is " + localChatDistance);
    }

    public void onDisable() {
        ipc.deregisterInterface("locchat");
        ipc.deregisterInterface("chatquery");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        // String highestRank = "default";
        // Player player = event.getPlayer();
        // int prio = 0;
        // Set<String> ranks = getConfig().getConfigurationSection("ranks").getKeys(false);
        // for (String rank : ranks) {
        //     ConfigurationSection rankData = getConfig().getConfigurationSection("ranks").getConfigurationSection(rank);
        //     if (player.hasPermission(rankData.getString("permission")) && rankData.getInt("priority") > prio) {
        //         prio = rankData.getInt("priority");
        //         highestRank = rank;
        //     }
        // }
        // ScoreboardManager manager = Bukkit.getScoreboardManager();
        // Scoreboard mainBoard = manager.getMainScoreboard();
        // System.out.println("Set scoreboard team of player " + player.getName() + " to " + highestRank);
        // if(mainBoard.getTeam(highestRank) != null) {
        //     mainBoard.getTeam(highestRank).addEntry(player.getName().toString());
        // }
        // else {
        //     System.out.println("Team not found on scoreboard!");
        // }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("modreq")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            
            if(args.length == 0) {
                sender.sendMessage("§cEnter /modreq followed by a description what you need help with.");
                return true;
            }
            
            String text = "";
            for(int i = 0; i < args.length; i++) {
                if(i > 0) text += " ";
                text += args[i];
            }

            Location loc = player.getLocation();
            ipc.sendMessage("modreq", player.getUniqueId() + "|" + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "|" + text);
            return true;
        }
        else if(command.getName().equals("unlocktutorialchat")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            ipc.sendMessage("unlocktutorialchat", player.getUniqueId().toString());
            return true;
        }
        else if(command.getName().equals("finishtutorial")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            ipc.sendMessage("finishtutorial", player.getUniqueId().toString());
            return true;
        }
        else if(command.getName().equals("ltr")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;

            if(args.length == 0) return true;
            Collection<Player> players = (Collection<Player>) getServer().getOnlinePlayers();
            String message = args[0];
            for(int i = 1; i < args.length; i++) message += " " + args[i];
            message = ChatColor.translateAlternateColorCodes('&', message);
            Location loc = player.getLocation();
            for(Player p: players) {
                Location pl = p.getLocation();
                if(pl.getWorld().getUID().equals(loc.getWorld().getUID()) && pl.distance(loc) < localChatDistance) {
                    p.sendMessage(message);
                }
            }
            return true;
        }
	else if(command.getName().equals("localchatdistance")) {
	    if(args.length == 0) {
		sender.sendMessage("§c/localchatdistance <distance>");
	    }
	    else {
		localChatDistance = Integer.valueOf(args[0]);
	    }
	}
        else if(command.getName().equals("localchatregion")) {
            if(args.length == 0) {
                sender.sendMessage("§c/localchatregion list|add|remove");
            }
            else if((args[0].equals("add") || args[0].equals("remove")) && args.length != 2) {
                sender.sendMessage("§c/localchatregion " + args[0] + " <regionname>");
            }
            else if(args[0].equals("list")) {
                if(args.length != 1) {
                    sender.sendMessage("§clist doesn't have any parameters");
                }
                else {
                    sender.sendMessage("§aLocal chat regions:");
                    for(String name: localChatRegions) {
                        sender.sendMessage("§e- " + name);
                    }
                }
            }
            else if(args[0].equals("add")) {
                if(localChatRegions.add(args[1])) {
                    sender.sendMessage("§a" + args[1] + " added to list of local chat regions.");
                    writeLocalChatRegionsToConfig();
                }
                else {
                    sender.sendMessage("§c" + args[1] + " is already in the list of local chat regions.");
                }
            }
            else if(args[0].equals("remove")) {
                if(localChatRegions.remove(args[1])) {
                    sender.sendMessage("§a" + args[1] + " removed from list of local chat regions.");
                    writeLocalChatRegionsToConfig();
                }
                else {
                    sender.sendMessage("§c" + args[1] + " is not in the list of local chat regions.");
                }
            }
            else {
                sender.sendMessage("§c/localchatregion list|add|remove");
            }
            return true;
        }
        return false;
    }

    private void writeLocalChatRegionsToConfig() {
        List<String> list = new ArrayList<>(localChatRegions);
        getConfig().set("localchatregions", list);
        saveConfig();
    }
    
    public void process(String channel, String message) {
        if(channel.equals("chatquery")) {
            StringTokenizer tk = new StringTokenizer(message, "|");
            if(tk.countTokens() != 4) return;
            String channelName = tk.nextToken();
            String mId = tk.nextToken();
            UUID playerId = UUID.fromString(tk.nextToken());
            String parameter = tk.nextToken();
            if(!parameter.equals("health")) return;

            Player player = getServer().getPlayer(playerId);
            if(player == null) return;

            double health = 20.0;
            if(player.getGameMode() == GameMode.SURVIVAL) health = player.getHealth();
            ipc.sendMessage("chatquery|" + channelName + "|" + mId + "|" + playerId.toString() + "|health=" + health);
        }
        else if(channel.equals("locchat")) {
            int idx = message.indexOf("|");
            if(idx == -1) return;
            
            String idList = message.substring(0, idx);
            int sidx = idList.indexOf(";");
            UUID playerId;
            Set<UUID> mutedIds = new HashSet<>();
            if(sidx == -1) {
                playerId = UUID.fromString(idList);
            }
            else {
                playerId = UUID.fromString(idList.substring(0, sidx));
                StringTokenizer tk = new StringTokenizer(idList.substring(sidx + 1), ",");
                while(tk.hasMoreTokens()) {
                    mutedIds.add(UUID.fromString(tk.nextToken()));
                }
            }

            Player player = getServer().getPlayer(playerId);
            if(player == null) return;
            Location loc = player.getLocation();

            message = message.substring(idx + 1);
            String greyMessage = "§7" + removeColorCodes(message);
            String darkGreyMessage = "§8" + removeColorCodes(message);
            if(!BlockUtils.isInRegion("no_unseen_local_chat_mark", loc)) {
                darkGreyMessage = "§c(unseen) " + darkGreyMessage;
                Long lgci = lastGlobalChatInfo.get(player.getUniqueId());
                if(lgci == null || System.currentTimeMillis() - lgci > 3600000) {
                    darkGreyMessage += " §c(start your message with /y for global chat)";
                    lastGlobalChatInfo.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
               
            Collection<Player> players = (Collection<Player>) getServer().getOnlinePlayers();
            List<Player> recipients = new ArrayList<>();
            List<Player> vanishedClosePlayers = new ArrayList<>();
            List<Player> monitoringFarPlayers = new ArrayList<>();
            
            String chatRegion = null;
            if(localChatRegions.size() != 0) {
                chatRegion = BlockUtils.findApplicableRegion(localChatRegions, player.getLocation());
            }

            UUID playerWorldId = player.getLocation().getWorld().getUID();
            
            for(Player p: players) {
                if(!p.getUniqueId().equals(player.getUniqueId())) {
                    boolean isInVicinity = false;
                    Location pl = p.getLocation();
                    if(pl.getWorld().getUID().equals(playerWorldId)) {
                        if(chatRegion != null) {
                            isInVicinity = BlockUtils.isInRegion(chatRegion, p.getLocation());
                        }
                        else {
                            isInVicinity = pl.distance(loc) < localChatDistance;
                        }
                    }
                    if(isInVicinity) {
                        if(isVanished(p)) {
                            vanishedClosePlayers.add(p);
                        }
                        else {
                            p.sendMessage(message);
                            recipients.add(p);
                        }
                    }
                    else if(p.hasPermission("cvchat.monitor.local") && false == mutedIds.contains(p.getUniqueId())) {
                        monitoringFarPlayers.add(p);
                    }
                }
            }

            {
                String msg = message.substring(message.indexOf('>') + 2);
                Bukkit.getPluginManager().callEvent(new SendLocal(player, msg));
            }

            player.sendMessage(recipients.size() == 0 ? darkGreyMessage : message);

            for(Player p: vanishedClosePlayers) {
                String m = message;
                if(recipients.size() == 0) m += " §4*";
                p.sendMessage(m);
            }

            TextComponent greyMessageOut;
            if(recipients.size() == 0) {
                greyMessage += " §4*";
                greyMessageOut = new TextComponent(greyMessage);
            } else {
                greyMessageOut = new TextComponent(greyMessage);
                greyMessageOut.addExtra(" ");
                TextComponent hover = new TextComponent("§3(" + recipients.size() + ")");
                String inRange = "";
                int i = recipients.size();
                for(Player p : recipients) {
                    inRange = inRange + p.getName();
                    i--;
                    if(i > 0) {
                        //inRange = inRange + ", ";
                        inRange = inRange + "\n";
                    }
                }
                hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(inRange)));
                greyMessageOut.addExtra(hover);


            }
            String iv = "";
            for(Player p : recipients) {
                if(iv.length() > 0) iv += ",";
                iv += p.getUniqueId().toString();
            }
            ipc.sendMessage("localmonitor|" + iv + "|" + greyMessage);
            for(Player p: monitoringFarPlayers) {
                p.spigot().sendMessage(greyMessageOut);
            }
            
            int gtidx = message.indexOf(">");
            if(gtidx != -1) {
                if(message.substring(gtidx + 2).equals("fus") && player.hasPermission("cvchat.thuum.fus")) {
                    fusRoDah(player, 1);
                }
                else if(message.substring(gtidx + 2).equals("fus ro") && player.hasPermission("cvchat.thuum.fus.ro")) {
                    fusRoDah(player, 2);
                }
                else if(message.substring(gtidx + 2).equals("fus ro dah") && player.hasPermission("cvchat.thuum.fus.ro.dah")) {
                    fusRoDah(player, 3);
                }
            }
            
        }
    }

    private static String[] colorCodes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "r"};
    public static String removeColorCodes(String text) {
        String ret = text;
        for(int i = 0; i < colorCodes.length; i++) {
            ret = ret.replace("§" + colorCodes[i], "");
        }
        return ret;
    }

    private boolean isVanished(Player player) {
        return cvvanish.isPlayerInvisible(player);
    }
    
    private void fusRoDah(Player dragonBorn, int level) {
        final double fusHoriStrength[] = {.5,2,7};
        final double fusVertStrength[] = {.5,.7,1.5};
        
        int distance = 5 * level;
        Vector heading = dragonBorn.getEyeLocation().getDirection();

        Vector blastVector = new Vector();
        blastVector.copy(heading).setY(0).normalize();
        blastVector.multiply(fusHoriStrength[level-1]).setY(fusVertStrength[level-1]);
        for(Entity victim : getAreaOfEffect(dragonBorn, 4, distance)) {
            victim.setVelocity(victim.getVelocity().add(blastVector));
        }

        dragonBorn.getWorld().playEffect(dragonBorn.getLocation(), Effect.GHAST_SHOOT, 0, distance + 10);
        if (level >= 2) {
            World world = dragonBorn.getWorld();
            List<Block> sight = dragonBorn.getLineOfSight(new HashSet<Material>(), 4);
            if (sight.size() >=0 ) world.createExplosion(sight.get(sight.size() - 1).getLocation(),0);
        }

        //if (level == 3){
            //List<Block> sight = dragonBorn.getLineOfSight(new HashSet<Material>(), 32);
            //for(int i = 8; i < 32 && i < sight.size() ; i += 6){
            //  Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Explosion(sight.get(i).getLocation(), 0, false), i/3);
            //}

        //}
    }

    private List<Entity> getAreaOfEffect(Player dragonBorn, int radius, int length){
        Location epicenter = dragonBorn.getEyeLocation();
        Vector heading = epicenter.getDirection();
        List<Entity> returnMe = new LinkedList<Entity>();
        
        length *= 2;
        for(Entity victim : dragonBorn.getNearbyEntities(length, length, length)){
            Vector dragonBornToVictim = victim.getLocation().subtract(epicenter).toVector();
            double dotProduct = dragonBornToVictim.dot(heading);
            
            if(dotProduct < 0) continue; // This entity is behind the dovahkiin
            if(dragonBornToVictim.lengthSquared() - dotProduct * dotProduct > radius*radius) continue; // Entity is too far laterally from the shout.
            
            returnMe.add(victim);
        }
        return returnMe;
    }
}
