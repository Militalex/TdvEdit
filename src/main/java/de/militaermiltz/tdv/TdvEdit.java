package de.militaermiltz.tdv;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.militaermiltz.tdv.commands.*;
import de.militaermiltz.tdv.commands.util.ShowPlaysoundTickable;
import de.militaermiltz.tdv.events.GeneralListener;
import de.militaermiltz.tdv.events.ResourcePackListener;
import de.militaermiltz.tdv.events.ServerResourcePackListener;
import de.militaermiltz.tdv.util.ServerPropertiesManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author Militalex
 * @version 1.9
 *
 * Tdv Edit Plugin.
 *
 */
public final class TdvEdit extends JavaPlugin {

    public static TdvEdit PLUGIN;
    public static ServerPropertiesManager propertiesManager;
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        //Save Default Config File if not existing
        if (!Files.exists(Paths.get("plugins/TdvEdit/config.yml"))) this.saveDefaultConfig();
        try {
            propertiesManager = new ServerPropertiesManager();
        }
        catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot load config.");
        }

        PLUGIN = this;
        final int pack = this.getConfig().getInt("PackBehave");

        //Commands
        Objects.requireNonNull(getCommand("modifyplaysound")).setExecutor(new ModifyPlaysoundCommand());
        Objects.requireNonNull(getCommand("commandprepend")).setExecutor(new CommandPrependCommand());
        Objects.requireNonNull(getCommand("crescendo")).setExecutor(new CrescendoCommand());
        Objects.requireNonNull(getCommand("resourcepack")).setExecutor(new ResourcePackCommand());
        Objects.requireNonNull(getCommand("givenote")).setExecutor(new GiveNoteCommand());
        Objects.requireNonNull(getCommand("modifycommandblockstates")).setExecutor(new ModifyCommandblockStatesCommand());
        Objects.requireNonNull(getCommand("showplaysoundstats")).setExecutor(new ShowPlaysoundCommand());


        //Events
        // request player to download server resourcepack
        if (pack == 1) getServer().getPluginManager().registerEvents(new ServerResourcePackListener(), this);

        // request player to download config resourcepack
        if (pack == 2) getServer().getPluginManager().registerEvents(new ResourcePackListener(), this);

        getServer().getPluginManager().registerEvents(new GeneralListener(), this);

        //Protocol Lib
        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.SET_COMMAND_BLOCK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (ShowPlaysoundTickable.exists()) {
                    ShowPlaysoundTickable.getInstance().setPlayerDirty(event.getPlayer());
                }
            }
        });

        Bukkit.getServer().getWorlds().forEach(world -> world.getEntities().stream().filter(
                entity -> entity.getScoreboardTags().contains("tdvedit_visualizer")).forEach(Entity::remove));

        this.getLogger().info("--------- TdvEdit successfully enabled. ---------");
    }

    @Override
    public void onDisable() {
        ShowPlaysoundTickable.staticStop();
    }
}
