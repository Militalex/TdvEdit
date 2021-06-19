package de.militaermiltz.tdv;

import de.militaermiltz.tdv.commands.CommandPrependCommand;
import de.militaermiltz.tdv.commands.CrescendoCommand;
import de.militaermiltz.tdv.commands.ModifyPlaysoundCommand;
import de.militaermiltz.tdv.events.ResourcepackListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 * @author Alexander Ley
 * @version 1.8
 *
 * Tdv Edit Plugin.
 *
 */
public final class TdvEdit extends JavaPlugin {

    @Override
    public void onEnable() {
        //Save Default Config File if not existing
        if (!Files.exists(Paths.get("plugins/TdvEdit/config.yml"))) this.saveDefaultConfig();

        final boolean resources = this.getConfig().getBoolean("resourcepack");

        //Commands
        Objects.requireNonNull(getCommand("modifyplaysound")).setExecutor(new ModifyPlaysoundCommand());
        Objects.requireNonNull(getCommand("commandprepend")).setExecutor(new CommandPrependCommand());
        Objects.requireNonNull(getCommand("crescendo")).setExecutor(new CrescendoCommand());

        //Events
        if (resources) getServer().getPluginManager().registerEvents(new ResourcepackListener(), this);

        this.getLogger().info("--------- TdvEdit successfully enabled. ---------");
    }

    @Override
    public void onDisable() {

    }
}
