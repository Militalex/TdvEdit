package de.militaermiltz.tdv.events;

import de.militaermiltz.tdv.commands.util.ShowPlaysoundTickable;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * This class is a Listener for several events collected here.
 *
 */
public class GeneralListener implements Listener {

    /**
     * Marks player dirty and refresh his armorstands if player using playsound visualization
     */
    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        final BlockState wildState = event.getClickedBlock().getState();
        if (event.hasBlock() && (wildState instanceof CommandBlock || wildState.getType() == Material.NOTE_BLOCK)  && ShowPlaysoundTickable.exists()){
            ShowPlaysoundTickable.getInstance().setPlayerDirty(event.getPlayer());
        }
    }

    /**
     * Remove player from visualization program if he is quit the server.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if (ShowPlaysoundTickable.exists()){
            ShowPlaysoundTickable.getInstance().removePlayer(event.getPlayer());
        }
    }
}
