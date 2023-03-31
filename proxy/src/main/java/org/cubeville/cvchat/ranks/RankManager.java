package org.cubeville.cvchat.ranks;

import java.util.*;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class RankManager
{
    private List<Rank> ranks;
    private Map<String, Prefix> prefixes;
    
    // TODO: Optionally add cache
    
    private static RankManager instance;
    public static RankManager getInstance() {
        return instance;
    }
    
    public RankManager(Configuration config, Configuration prefixConfig) {
        instance = this;

        {
            ranks = new ArrayList<>();
            //playerPostfix = new HashMap<>();
            //playerPriority = new HashMap<>();
            
            Collection<String> rankNames = config.getKeys();
            for(String rankName: rankNames) {
                Configuration rankConfig = (Configuration) config.get(rankName);
                Rank rank = new Rank(rankConfig.getString("postfix"),
                                     rankConfig.getString("permission"),
                                     rankConfig.getInt("priority"),
                                     rankConfig.getString("color"));
                ranks.add(rank);
            }
        }

        {
            prefixes = new HashMap<>();
            Collection<String> prefixNames = prefixConfig.getKeys();
            for(String prefixName: prefixNames) {
                Configuration c = (Configuration) prefixConfig.get(prefixName);
                Prefix prefix = new Prefix(c.getString("prefix"), c.getString("permission"));
                prefixes.put(prefixName, prefix);
            }

        }
    }

    public Map<String, String> getPossiblePrefixes(ProxiedPlayer player) {
        Map<String, String> ret = new HashMap<>();
        for(String prefix: prefixes.keySet()) {
            if(player.hasPermission(prefixes.get(prefix).getPermission())) {
                ret.put(prefix, prefixes.get(prefix).getPrefix());
            }
        }
        return ret;
    }

    public boolean isPrefixPermitted(ProxiedPlayer player, String prefix) {
        if(player.hasPermission("cvchat.customprefix")) return true;
        if(Objects.equals(prefix, "")) return true;
        for(Prefix p: prefixes.values()) {
            if(p.getPrefix().equals(prefix)) {
                return player.hasPermission(p.getPermission());
            }
        }
        return false;
    }
    
    private Rank getRank(ProxiedPlayer player) {
        int prio = 0;
        Rank ret = null;
        for(Rank rank: ranks) {
            if((rank.getPermission().equals("default") || player.hasPermission(rank.getPermission())) && rank.getPriority() > prio) {
                prio = rank.getPriority();
                ret = rank;
            }
        }
        return ret;
    }
    
    public String getPostfix(CommandSender sender) {
        if(!(sender instanceof ProxiedPlayer)) return "CO";
        ProxiedPlayer player = (ProxiedPlayer) sender;
        return getRank(player).getPostfix();
    }

    public int getPriority(ProxiedPlayer player) {
        return getRank(player).getPriority();
    }

    public String getColor(ProxiedPlayer player) {
        return getRank(player).getColor();
    }
}
