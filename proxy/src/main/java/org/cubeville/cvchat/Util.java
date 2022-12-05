package org.cubeville.cvchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.StringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.playerdata.PlayerDataManager;

import org.cubeville.cvvanish.CVVanish;

public class Util
{
    public static String joinStrings(String[] parts, int offset) {
        String ret = "";
        for(int i = offset; i < parts.length; i++) {
            if(i > offset) ret += " ";
            ret += parts[i];
        }
        return ret;
    }

    public static String[] removeEmptyStrings(String[] args) {
        int nr = 0;
        for(int i = 0; i < args.length; i++) {
            if(args[i].length() > 0) nr++;
        }
        String ret[] = new String[nr];
        int c = 0;
        for(int i = 0; i < nr; i++) {
            while(args[c].length() == 0) {
                c++;
            }
            ret[i] = args[c];
            c++;
        }
        return ret;
    }

    public static String removeSectionSigns(String text) {
        text.replace("§", "");
        return text;
    }

    public static boolean getBooleanProperty(String text) {
        if(text.indexOf(':') == -1) return false;
        String s = text.substring(text.indexOf(':') + 1);
        s = s.trim();
        if(s.equals("true")) return true;
        return false;
    }

    public static String getStringProperty(String text) {
        if(text.indexOf(':') == -1) return null;
        String s = text.substring(text.indexOf(':') + 1);
        return s.trim();
    }

    public static String getPropertyName(String text) {
        if(text.indexOf(":") == -1) return null;
        return text.substring(0, text.indexOf(':')).trim();
    }

    public static void saveFile(File file, List<String> text) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for(String s: text) {
                writer.write(s);
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException exceptin) {}
        finally {
            try { fileWriter.close(); } catch (Exception e) {}
        }
    }

    public static List<String> readFile(File file) {
        if(!file.exists()) return null;
        List<String> ret = new ArrayList<>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            while(true) {
                String l = reader.readLine();
                if(l == null) break;
                ret.add(l);
            }
            reader.close();
        }
        catch(IOException exceptin) {}
        finally {
            try { fileReader.close(); } catch (Exception e) {}
        }
        return ret;
    }

    private static String[] colorCodes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "r"};
    private static final Pattern colorCodesHex = Pattern.compile("&#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})");
    
    public static String translateAlternateColorCodes(String text) {
        String retp = text;
        for(int i = 0; i < colorCodes.length; i++) {
            retp = retp.replace("&" + colorCodes[i], "§" + colorCodes[i]);
        }
        Matcher matcher = colorCodesHex.matcher(retp);
        StringBuilder ret = new StringBuilder();
        while(matcher.find()) {
            String col = matcher.group(1);
            if(col.length() == 6) {
                col = "§x§" + col.charAt(0) + "§" + col.charAt(1) + "§" + col.charAt(2) + "§" + col.charAt(3) + "§" + col.charAt(4) + "§" + col.charAt(5);
            }
            else {
                col = "§x§" + col.charAt(0) + "§" + col.charAt(0) + "§" + col.charAt(1) + "§" + col.charAt(1) + "§" + col.charAt(2) + "§" + col.charAt(2);
            }
            matcher.appendReplacement(ret, col);
        }
        return matcher.appendTail(ret).toString();
    }

    public static String removeColorCodes(String text) {
        String ret = text;
        for(int i = 0; i < colorCodes.length; i++) {
            ret = ret.replace("§" + colorCodes[i], "");
        }
        return ret;
    }
    
    public static boolean playerIsHidden(ProxiedPlayer player) {
        return playerIsHidden(player.getUniqueId());
    }

    public static boolean playerIsHidden(UUID playerId) {
        return CVVanish.getInstance().isPlayerInvisible(playerId);
    }

    public static boolean playerIsUnlisted(ProxiedPlayer player) {
        return playerIsUnlisted(player.getUniqueId());
    }

    public static boolean playerIsUnlisted(UUID playerId) {
        return CVVanish.getInstance().isPlayerUnlisted(playerId);
    }

    private static boolean isSamePlayer(CommandSender watchingPlayer, ProxiedPlayer watchedPlayer) {
        if(!(watchingPlayer instanceof ProxiedPlayer)) return false;
        ProxiedPlayer p = (ProxiedPlayer) watchingPlayer;
        return p.getUniqueId().equals(watchedPlayer.getUniqueId());
    }
    
    private static PlayerDataManager getPDM() {
        return PlayerDataManager.getInstance();
    }

    private static boolean outranksOrEqual(CommandSender watchingPlayer, ProxiedPlayer watchedPlayer) {
        if(!(watchingPlayer instanceof ProxiedPlayer)) return true; // console outranks everyone
        return getPDM().outranksOrEqual(((ProxiedPlayer) watchingPlayer).getUniqueId(), watchedPlayer.getUniqueId());
    }
    
    public static boolean getPlayerVisibilityFor(CommandSender watchingPlayer, ProxiedPlayer watchedPlayer) {
        if(!(watchingPlayer instanceof ProxiedPlayer)) return true;              // Console sees everything
        if(!(playerIsUnlisted(watchedPlayer))) return true;                      // Everyone sees listed players
        if(isSamePlayer(watchingPlayer, watchedPlayer)) return true;             // Everyone see themselves
        if(outranksOrEqual(watchingPlayer, watchedPlayer)) {
            if(watchedPlayer.hasPermission("cvchat.visibilityexemption")) {      // Players with exemption are not visible even to higher ranked staffers
                return watchingPlayer.hasPermission("cvchat.visibilityexemptionoverride"); // ...unless the higher ranks staffer overrides this
            }
            return true; // Everyone can see staffers of same or lower rank
        }
        return false;                                                            // nope
    }

    public static boolean getPlayerInvisibilityStatusFor(CommandSender watchingPlayer, ProxiedPlayer watchedPlayer) {
        if(!(watchingPlayer instanceof ProxiedPlayer)) return playerIsHidden(watchedPlayer);               // Console always sees actual status of player
        if(outranksOrEqual(watchingPlayer, watchedPlayer)) return playerIsHidden(watchedPlayer); // Staffers see the actual status of same or lower rank
        return false;                                                                                      // Everyone else don't see shit
    }

    public static boolean getPlayerUnlistedStatusFor(CommandSender watchingPlayer, ProxiedPlayer watchedPlayer) {
        if(!(watchingPlayer instanceof ProxiedPlayer)) return playerIsUnlisted(watchedPlayer);               // Console always sees actual status of player
        if(outranksOrEqual(watchingPlayer, watchedPlayer)) return playerIsUnlisted(watchedPlayer); // Staffers see the actual status of same or lower rank
        return false;                                                                                        // Everyone else don't see shit
    }
    
    public static List<ProxiedPlayer> getPlayersWithPermission(String permission) {
        List<ProxiedPlayer> ret = new ArrayList<>();
        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()) {
            if(player.hasPermission(permission)) {
                ret.add(player);
            }
        }
        return ret;
    }
}
