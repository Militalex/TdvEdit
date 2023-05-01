package de.militaermiltz.tdv.events;

import de.militaermiltz.tdv.TdvEdit;
import de.militaermiltz.tdv.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Militalex
 * @version 1.0
 *
 * This class defines ResourcePack Messages when player joins the world.
 *
 */
public class ResourcePackListener implements Listener {

    public static void showMessage(Player player){
        //Check if pack.txt exists.
        final Path pack = Paths.get("plugins/TdvEdit/pack.txt");
        FileUtil.createIfNotExists(pack);


        //Get Pack info from server.properties
        final String[] packInfo;
        try {
            packInfo = getPackFromServerConfig();
        }
        catch (IOException ioException) {
            player.sendMessage(ChatColor.RED + "Cannot get Pack Info from server.properties.");
            ioException.printStackTrace();
            return;
        }

        //If components of the server resourcepack are not given.
        if (packInfo[0] == null) {
            player.sendMessage(ChatColor.RED + "Resourcepack is missing.");
            return;
        }
        else if (packInfo[1] == null){
            player.sendMessage(ChatColor.RED + "SHA-1 Key is missing.");
            return;
        }

        //Put SHA-1 Key in pack.txt when empty (Only first time).
        if (FileUtil.isEmpty(pack)){
            try {
                Files.write(pack, packInfo[1].getBytes());
            }
            catch (IOException ioException) {
                player.sendMessage(ChatColor.RED + "Cannot access pack.txt.");
                ioException.printStackTrace();
                return;
            }
        }

        //Read pack.txt
        List<String> packContent;
        try {
            packContent = Files.readAllLines(pack);
        }
        catch (IOException ioException) {
            player.sendMessage(ChatColor.RED + "pack.txt is not readable.");
            ioException.printStackTrace();
            return;
        }

        //Compare SHA-1 Key from server.properties and pack.txt
        if (!packContent.get(0).equals(packInfo[1])){
            packContent = new ArrayList<>(Collections.singleton(packInfo[1]));
            try {
                //Refresh SHA-1 Key in file and delete all player names.
                Files.write(pack, packContent, StandardOpenOption.CREATE);
            }
            catch (IOException ioException) {
                player.sendMessage(ChatColor.RED + "Cannot refresh pack.txt.");
                ioException.printStackTrace();
                return;
            }
        }

        //Check if player Name is not in pack.txt
        if (!packContent.contains(player.getName())){
            Bukkit.getServer().dispatchCommand(player, "tellraw @s [\"\"," +
                    "{\"text\":\"The animation pack has been changed. Click here to download the newest version and load it manually.\",\"color\":\"red\"}," +
                    "{\"text\":\"\\n\"},{\"text\":\"< Download >\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + packInfo[0]+ "\"}}]");

            //Add player Name to pack.txt List (Mark player as seen).
            packContent.add(player.getName());
            try {

                Files.write(pack, packContent, StandardOpenOption.CREATE);
            }
            catch (IOException ioException) {
                player.sendMessage(ChatColor.RED + "Cannot refresh pack.txt.");
                ioException.printStackTrace();
            }
        }
        else {
            //If player is in pack.txt
            Bukkit.getServer().dispatchCommand(player, "tellraw @s [\"\"," +
                    "{\"text\":\"Note that the server resourcepack is not complete. You find the animation Pack here:\",\"color\":\"light_purple\"}," +
                    "{\"text\":\"\\n\"},{\"text\":\"< Click here to download >\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + packInfo[0]+ "\"}}]");

        }
    }

    /**
     * Extracts server resourcePack information from server.properties
     * @throws IOException if an I/O error occurs opening source.
     */
    private static String[] getPackFromServerConfig() throws IOException {
        final FileConfiguration config = TdvEdit.PLUGIN.getConfig();
        final String[] packInfo = new String[2];

        packInfo[0] = config.getString("Packlink");
        packInfo[1] = config.getString("SHA-1");

        return packInfo;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        showMessage(player);
    }
}
