package de.militaermiltz.tdv.commands.util;

import de.militaermiltz.tdv.TdvEdit;
import de.militaermiltz.tdv.commands.CommandUtil;
import de.militaermiltz.tdv.util.BukkitTickable;
import de.militaermiltz.tdv.util.ExLocation;
import de.militaermiltz.tdv.util.HomogenTuple;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Militalex
 * @version 1.5
 *
 * This singleton class includes the playsound visualization magic.
 * This Tickable manages itself automatically. Starting and Stopping manually via Bukkit
 * Tickable interface may cause problems.
 *
 */
public class ShowPlaysoundTickable extends BukkitTickable {

    // Singleton Pattern
    private static ShowPlaysoundTickable tickable = null;
    public static ShowPlaysoundTickable getInstance(){
        if (!exists()){
            tickable = new ShowPlaysoundTickable(TdvEdit.PLUGIN);
        }
        return tickable;
    }
    // Singleton Pattern

    /**
     * Contains all summoned Armorstands
     */
    public static final List<ArmorStand> armorStands = new ArrayList<>();

    /**
     * Predicate object to identify if entity belong to playsound visualization engine.
     */
    public static final Predicate<Entity> isPlaysoundShower = entity ->
            entity instanceof ArmorStand && armorStands.contains(entity);

    /**
     * Shutdown tickable
     */
    public static void staticStop(){
        if (exists() && tickable.getStarted()){
            tickable.stop();
        }
    }

    /**
     * @return Returns if tickable instance exists.
     */
    public static boolean exists(){
        return tickable != null;
    }

    /**
     * Contains all visualizing players.
     */
    private final List<Player> playerList = new ArrayList<>();

    /**
     * Contains information which players armorstand should be refreshed
     */
    private final List<Player> dirtyPlayers = new ArrayList<>();

    /**
     * Private constructor for Singleton Pattern
     * @param plugin is needed to use Bukkit.getScheduler.runTaskTimer(plugin, ...)
     */
    private ShowPlaysoundTickable(Plugin plugin) {
        super(plugin);
    }

    /**
     * Add player and starts visualizing algorithm if needed.
     */
    public void addPlayer(Player player){
        if (playerList.contains(player)) return;

        playerList.add(player);
        if (!getStarted()) start(0, 4);
    }

    public boolean containsPlayer(Player player){
        return playerList.contains(player);
    }

    /**
     * Removes player and stops visualizing algorithm.
     */
    public void removePlayer(Player player){
        if (!playerList.contains(player)) return;

        playerList.remove(player);
        if (playerList.isEmpty()) stop();
    }

    /**
     * Mark player that his armorstands should refresh
     */
    public void setPlayerDirty(Player player){
        if (!playerList.contains(player)) return;
        dirtyPlayers.add(player);
    }

    /**
     * Visualizing tick algorithm
     */
    @Override
    public void tick() {
        // Contains information about places armorstand should stay alive
        final List<BoundingBox> boxes = new ArrayList<>(playerList.size());

        playerList.forEach(player -> {
            final Location ploc = player.getLocation();

            //Calculate fromm too positions
            ExLocation from = new ExLocation(ploc);
            from.subtract(8, 3, 8);
            ExLocation to = new ExLocation(ploc);
            to.add(8, 3, 8);

            final HomogenTuple<Location> fromToo = CommandUtil.transformLocation(from, to);
            from = new ExLocation(fromToo.getKey());
            to = new ExLocation(fromToo.getValue());

            for (int x = from.getBlockX(); x <= to.getBlockX(); x++){
                for (int y = from.getBlockY(); y <= to.getBlockY(); y++){
                    for (int z = from.getBlockZ(); z <= to.getBlockZ(); z++){
                        tryVisualize(player, new Location(from.getWorld(), x, y, z));
                    }
                }
            }

            boxes.add(new BoundingBox(ploc.getBlockX() - 10, ploc.getBlockY() - 4, ploc.getBlockZ() - 10,
                    ploc.getBlockX() + 10, ploc.getBlockY() + 4, ploc.getBlockZ() + 10));

            // refresh players armorstands
            if (dirtyPlayers.contains(player)){
                dirtyPlayers.remove(player);
                armorStands.stream()
                        .filter(armorStand ->
                                armorStand.getBoundingBox().overlaps(new BoundingBox(ploc.getBlockX() - 8,
                                ploc.getBlockY() - 8, ploc.getBlockZ() - 8, ploc.getBlockX() + 8,
                                ploc.getBlockY() + 8, ploc.getBlockZ() + 8)))
                        .forEach(armorStand ->
                            armorStand.setCustomName(getSoundDataString(player, armorStand.getLocation().getBlock().getState(), false))
                        );
            }
        });

        // Kill armorstands which are outside
        final List<ArmorStand> toRemove = armorStands.stream()
                .filter(armorStand -> {
                    if (armorStand == null || armorStand.isDead()) return true;

                    // kill if no command or noteblock
                    final BlockState entSta = armorStand.getLocation().getBlock().getState();
                    if (!(entSta instanceof CommandBlock || entSta.getType() == Material.NOTE_BLOCK)) return true;

                    // kill if outside of range
                    for (BoundingBox box : boxes) {
                        if (armorStand.getBoundingBox().overlaps(box)) {
                            return false;
                        }
                    }
                    return true;
                })
                .peek(stand -> {
                    if (stand != null) stand.remove();
                })
                .toList();

        armorStands.removeAll(toRemove);
    }

    /**
     * Stops Tickable and kills all visualization helper armorstands and shows a message into console.
     */
    @Override
    public void stop() {
        super.stop();
        tickable = null;

        playerList.forEach(playerList::remove);

        armorStands.forEach(Entity::remove);
    }

    /**
     * Tries to visualize information on supported blocks (Command- and Noteblocks)
     * @param player Current player
     * @param location Current Location which should one around the player
     */
    @SuppressWarnings("ConstantConditions")
    private void tryVisualize(Player player, Location location){
        final BlockState wildState = location.getBlock().getState();

        // if block is commandblock or noteblock
        if (wildState instanceof CommandBlock | wildState.getBlockData().getMaterial() == Material.NOTE_BLOCK){
            //calculate possible visualization armorstands
            final Collection<Entity> stands = location.getWorld().getNearbyEntities(new BoundingBox(location.getBlockX(), location.getBlockY(),
                            location.getBlockZ(),location.getBlockX() + 1.0, location.getBlockY() + 1.0,
                    location.getBlockZ() + 1.0), isPlaysoundShower);

            // if commandblock/noteblock has no visualization armorstand and block has music data
            if (stands.isEmpty() && hasSoundData(player, wildState)){
                final String data = getSoundDataString(player, wildState, true);

                final ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(new Location(location.getWorld(),
                        location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5), EntityType.ARMOR_STAND);
                stand.setCustomNameVisible(true);
                stand.setGravity(false);
                stand.setSilent(true);
                stand.setInvulnerable(true);
                stand.setSmall(true);
                stand.setInvisible(true);
                stand.setBasePlate(false);
                stand.setPersistent(true);
                stand.setCustomName(data);
                stand.addScoreboardTag("tdvedit_visualizer");

                armorStands.add(stand);
            }
        }
    }

    /**
     * Checks if block is a valid playsound commandblock or a noteblock.
     */
    private boolean hasSoundData(Player player, BlockState wildState){
        return (wildState instanceof CommandBlock && CommandUtil.isCommandWithPlaysound(player, (CommandBlock)wildState))
                || wildState.getType() == Material.NOTE_BLOCK;
    }

    /**
     * Extracts sound data from valid (playsound) commandblock or noteblock and
     * format it for using as CustomName.
     * @param tested should playsound criteria be tested or not
     * @return Return well formated sound data to use as
     * CustomName for visualization armorstands. Returns "" if no valid data can be extracted.
     */
    private String getSoundDataString(Player player, BlockState wildState, boolean tested){
        // Build commandblock playsound data String
        if (wildState instanceof CommandBlock && (tested || CommandUtil.isCommandWithPlaysound(player, (CommandBlock)wildState))){
            final HomogenTuple<Double> volpitch = CommandUtil.extractVolumePitch((CommandBlock)wildState);
            final double volume = volpitch.getKey();
            final double pitch = volpitch.getValue();
            final String note = CommandUtil.getNoteFromPitch(pitch);

            return "" + ChatColor.LIGHT_PURPLE + volume + " " + ChatColor.GOLD +
                    pitch + ((note.equals("")) ? "" : ChatColor.AQUA + "/" + note);
        }

        // Build noteblock playsound data String
        if (wildState.getType() == Material.NOTE_BLOCK){
            final int clicks = Integer.parseInt(CommandUtil.getBlockStates(wildState).get("note"));
            final String note = CommandUtil.getNoteFromPitch(CommandUtil.getPitchFromClicks((clicks)));

            return "" + ChatColor.GOLD +
                    clicks + ((note.equals("")) ? "" : ChatColor.AQUA + "/" + note);
        }

        return "";
    }
}
