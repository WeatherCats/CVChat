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
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.myzelyam.api.vanish.VanishAPI;

import org.cubeville.commons.utils.PlayerUtils;
import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CVChat extends JavaPlugin implements Listener, IPCInterface
{
    private List<LocalRegion> localRegions;
    private Map<ProtectedRegion, String> regionChatPrefixMap;
    private WorldGuardPlugin worldGuard;
    CVIPC ipc;
    
    @SuppressWarnings("unchecked")
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);

        ipc = (CVIPC) pm.getPlugin("CVIPC");
        ipc.registerInterface("locchat", this);
        ipc.registerInterface("chatquery", this);
        
        ConfigurationSerialization.registerClass(LocalRegion.class);
        
        localRegions = (List<LocalRegion>) getConfig().get("LocalRegions");
        if(localRegions == null) { localRegions = new ArrayList<LocalRegion>(); }
        regionChatPrefixMap = new HashMap<ProtectedRegion, String>();
        worldGuard = (WorldGuardPlugin) pm.getPlugin("WorldGuard");
        if(worldGuard == null) { return; }
        for(LocalRegion localRegion: localRegions) {
            World world = Bukkit.getWorld(localRegion.getWorldName());
            if(world == null) { continue; }
            RegionManager regionManager = worldGuard.getRegionManager(world);
            if(regionManager == null) { continue; }
            ProtectedRegion region = regionManager.getRegion(localRegion.getRegionName());
            if(region == null) { continue; }
            regionChatPrefixMap.put(region, localRegion.getChatPrefix().replaceAll("&", "§"));
        }
    }

    public void onDisable() {
        getConfig().set("LocalRegions", localRegions);
        saveConfig();
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

    @SuppressWarnings("unchecked")
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
                if(pl.getWorld().getUID().equals(loc.getWorld().getUID()) && pl.distance(loc) < 55) {
                    p.sendMessage(message);
                }
            }
            return true;
        }
        else if(command.getName().equals("addlocalregion")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("ERROR: Only Players can use this command!");
                return false;
            }
            Player player = (Player) sender;
            if(!player.hasPermission("cvchat.local.regionchat")) {
                player.sendMessage("§fUnknown command! Type \"/help\" for help.");
                return false;
            }
            if(args.length <= 2) {
                player.sendMessage("§cSyntax: /addlocalregion <region> <chat prefix...>");
                return true;
            }
            String[] copyOfArgs = args.clone();
            for(int index = 1; index < copyOfArgs.length; index++) {
                copyOfArgs[index] = copyOfArgs[index].replaceAll("&", "§");
            }
            String chatPrefixPrimary = args[1];
            String chatPrefixSecondary = copyOfArgs[1];
            for(int index = 2; index < args.length; index++) {
                chatPrefixPrimary += (" " + args[index]);
                chatPrefixSecondary += (" " + copyOfArgs[index]);
            }
            LocalRegion localRegion = new LocalRegion(player.getWorld().getName(), args[0], chatPrefixPrimary);
            if(worldGuard == null) {
                player.sendMessage("§cERROR: No WorldGuard plugin found!");
                return true;
            }
            RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
            if(regionManager == null) {
                player.sendMessage("§cERROR: No RegionManager found for this world!");
                return true;
            }
            ProtectedRegion protectedRegion = regionManager.getRegion(args[0]);
            if(protectedRegion == null) {
                player.sendMessage("§cNo region found!");
                return true;
            }
            regionChatPrefixMap.put(protectedRegion, chatPrefixSecondary);
            localRegions.add(localRegion);
            player.sendMessage("§aChat prefix enabled on region!");
            return true;
        }
        else if(command.getName().equals("removelocalregion")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("ERROR: Only Players can use this command!");
                return false;
            }
            Player player = (Player) sender;
            if(!player.hasPermission("cvchat.local.regionchat")) {
                player.sendMessage("§fUnknown command! Type \"/help\" for help.");
                return false;
            }
            if(args.length != 1) {
                player.sendMessage("§cSyntax: /removelocalregion <region>");
                return true;
            }
            for(int index = localRegions.size() - 1; index >= 0; index--) {
                if(localRegions.get(index).getWorldName().equals(player.getWorld().getName())) {
                    if(localRegions.get(index).getRegionName().equalsIgnoreCase(args[0])) {
                        localRegions.remove(index);
                    }
                }
            }
            if(worldGuard == null) {
                player.sendMessage("§cERROR: No WorldGuard plugin found!");
                return true;
            }
            RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
            if(regionManager == null) {
                player.sendMessage("§cERROR: No region manager for this world!");
                return true;
            }
            ProtectedRegion protectedRegion = regionManager.getRegion(args[0]);
            if(protectedRegion == null) {
                player.sendMessage("§cNo region found by that name!");
                return true;
            }
            if(regionChatPrefixMap.containsKey(protectedRegion)) {
                regionChatPrefixMap.remove(protectedRegion);
            }
            player.sendMessage("§aLocal chat removed for region.");
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
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
            UUID chatSenderId;
            Set<UUID> mutedIds = new HashSet<>();
            if(sidx == -1) {
                chatSenderId = UUID.fromString(idList);
            }
            else {
                chatSenderId = UUID.fromString(idList.substring(0, sidx));
                StringTokenizer tk = new StringTokenizer(idList.substring(sidx + 1), ",");
                while(tk.hasMoreTokens()) {
                    mutedIds.add(UUID.fromString(tk.nextToken()));
                }
            }
            message = message.substring(idx + 1);
            String greyMessage = "§7" + removeColorCodes(message);
            String darkGreyMessage = "§8" + removeColorCodes(message);
            
            Player chatSender = getServer().getPlayer(chatSenderId);
            if(chatSender == null) return;
            Location senderLocation = chatSender.getLocation();
            Set<ProtectedRegion> playerRegions = PlayerUtils.getRegionsAtPlayerLocation(chatSender);
            ProtectedRegion selectedRegion = null;
            for(ProtectedRegion playerRegion: playerRegions) {
                if(regionChatPrefixMap.keySet().contains(playerRegion)) {
                    selectedRegion = playerRegion;
                    break;
                }
            }
            
            if(selectedRegion == null) {
                Collection<Player> serverPlayers = (Collection<Player>) getServer().getOnlinePlayers();
                int recipientCount = 0;
                List<Player> vanishedClosePlayers = new ArrayList<Player>();
                for(Player serverPlayer: serverPlayers) {
                    if(!serverPlayer.getUniqueId().equals(chatSender.getUniqueId())) {
                        Location serverPlayerLocation = serverPlayer.getLocation();
                        if(serverPlayerLocation.getWorld().getUID().equals(senderLocation.getWorld().getUID()) && serverPlayerLocation.distance(senderLocation) < 55.0D) {
                            if(isVanished(serverPlayer)) {
                                vanishedClosePlayers.add(serverPlayer);
                            }
                            else {
                                serverPlayer.sendMessage(message);
                                recipientCount++;
                            }
                        }
                    }
                }
                chatSender.sendMessage(recipientCount == 0 ? darkGreyMessage : message);
                for(Player vanishedClosePlayer: vanishedClosePlayers) {
                    if(recipientCount == 0) { vanishedClosePlayer.sendMessage(message + " §4*"); }
                    else { vanishedClosePlayer.sendMessage(message); }
                }
                if(recipientCount == 0) { greyMessage += " §4*"; }
                ipc.sendMessage("localmonitor|" + greyMessage);
                for(Player serverPlayer: serverPlayers) {
                    Location serverLocation = serverPlayer.getLocation();
                    boolean sameWorld = serverLocation.getWorld().getUID().equals(senderLocation.getWorld().getUID());
                    if(!sameWorld || serverLocation.distance(senderLocation) >= 55.0D) {
                        if(serverPlayer.hasPermission("cvchat.monitor.local") && !mutedIds.contains(serverPlayer.getUniqueId())) {
                            serverPlayer.sendMessage(greyMessage);
                        }
                    }
                }
            }
            else {
                List<Player> regionPlayers = PlayerUtils.getPlayersInsideRegion(selectedRegion, chatSender.getWorld());
                List<Player> nonRegionPlayers = PlayerUtils.getPlayersOutsideRegion(selectedRegion, chatSender.getWorld());
                List<Player> vanishedRegionPlayers = new ArrayList<Player>();
                List<Player> visibleRegionPlayers = new ArrayList<Player>();
                for(Player regionPlayer: regionPlayers) {
                    if(regionPlayer.getUniqueId().equals(chatSender.getUniqueId())) { continue; }
                    if(isVanished(regionPlayer)) { vanishedRegionPlayers.add(regionPlayer); }
                    else { visibleRegionPlayers.add(regionPlayer); }
                }
                String chatPrefix = regionChatPrefixMap.get(selectedRegion) + " ";
                greyMessage = chatPrefix + greyMessage;
                darkGreyMessage = chatPrefix + darkGreyMessage;
                message = chatPrefix + message;
                for(Player player: visibleRegionPlayers) {
                    player.sendMessage(message);
                }
                chatSender.sendMessage(visibleRegionPlayers.size() == 0 ? darkGreyMessage : message);
                if(visibleRegionPlayers.size() == 0) {
                    greyMessage = greyMessage + (visibleRegionPlayers.size() == 0 ? " §4*" : "");
                    message = message + (visibleRegionPlayers.size() == 0 ? " §4*" : "");
                }
                for(Player player: vanishedRegionPlayers) {
                    player.sendMessage(message);
                }
                ipc.sendMessage("localmonitor|" + greyMessage);
                for(Player player: nonRegionPlayers) {
                    if(player.hasPermission("cvchat.monitor.local") && !mutedIds.contains(player.getUniqueId())) {
                        player.sendMessage(greyMessage);
                    }
                }
            }

            
            int gtidx = message.indexOf(">");
            if(gtidx != -1) {
                if(message.substring(gtidx + 2).equals("fus") && chatSender.hasPermission("cvchat.thuum.fus")) {
                    fusRoDah(chatSender, 1);
                }
                else if(message.substring(gtidx + 2).equals("fus ro") && chatSender.hasPermission("cvchat.thuum.fus.ro")) {
                    fusRoDah(chatSender, 2);
                }
                else if(message.substring(gtidx + 2).equals("fus ro dah") && chatSender.hasPermission("cvchat.thuum.fus.ro.dah")) {
                    fusRoDah(chatSender, 3);
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
        return VanishAPI.isInvisible(player);
    }
    
    private void fusRoDah(Player dragonBorn, int level) {
        System.out.println("Fus ro level " + level);
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
