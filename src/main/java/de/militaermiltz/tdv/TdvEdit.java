package de.militaermiltz.tdv;

import de.militaermiltz.tdv.commands.*;
import de.militaermiltz.tdv.events.ResourcePackListener;
import de.militaermiltz.tdv.util.ServerPropertiesManager;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author Alexander Ley
 * @version 1.8
 *
 * Tdv Edit Plugin.
 *
 */
public final class TdvEdit extends JavaPlugin {

    public static TdvEdit PLUGIN;
    public static ServerPropertiesManager propertiesManager;

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
        Objects.requireNonNull(getCommand("calibratemusic")).setExecutor(new CalibrationCommand());

        //Events
        if (resources) getServer().getPluginManager().registerEvents(new ResourcePackListener(), this);

        this.getLogger().info("--------- TdvEdit successfully enabled. ---------");
    }

    @Override
    public void onDisable() {

    }
}
