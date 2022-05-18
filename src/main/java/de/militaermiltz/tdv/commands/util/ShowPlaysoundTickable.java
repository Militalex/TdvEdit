package de.militaermiltz.tdv.commands.util;

import de.militaermiltz.tdv.TdvEdit;
import de.militaermiltz.tdv.commands.CommandUtil;
import de.militaermiltz.tdv.util.BukkitTickable;
import de.militaermiltz.tdv.util.ExLocation;
import de.militaermiltz.tdv.util.HomogenTuple;
import org.bukkit.Bukkit;
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
 * @author Alexander Ley
 * @version 1.0
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
     * Predicate object to identify if entity belong to playsound visualization engine.
     */
    public static final Predicate<Entity> isPlaysoundShower = entity ->
            entity instanceof ArmorStand && entity.getScoreboardTags().contains("tdvEdit_visualizer");

    /**
     * Shutdwon tickable
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
     * Marks tickable to refresh armorstands when perfoming next tick.
     */
    private boolean isDirty = false;

    /**
     * Contains all visualizing players.
     */
    private final List<Player> playerList = new ArrayList<>();

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
     * Marks that armorstand should be refreshed and executes next tick immediately.
     */
    public void markDirty(){
        isDirty = true;
        tick();
    }

    /**
     * Visualizing tick algorithm
     */
    @Override
    public void tick() {
        // Contains all currently armorstands which are in players environment.
        final ArrayList<Entity> actStandList = new ArrayList<>();

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

            // add surrounded armorstands
            actStandList.addAll(player.getWorld().getNearbyEntities(new BoundingBox(ploc.getBlockX() - 10, ploc.getBlockY() - 4, ploc.getBlockZ() - 10,
                    ploc.getBlockX() + 10, ploc.getBlockY() + 4, ploc.getBlockZ() + 10), isPlaysoundShower));
        });

        // Kill armorstands which are outside or kill all if algorithm should spawn it new
        Bukkit.getWorlds().forEach(world -> world.getEntities().stream().filter(isPlaysoundShower).filter(entity ->
                !actStandList.contains(entity) || isDirty).forEach(Entity::remove));
        isDirty = false;
    }

    /**
     * Stops Tickable and kills all visualization helper armorstands and shows a message into console.
     */
    @Override
    public void stop() {
        super.stop();
        tickable = null;

        playerList.forEach(playerList::remove);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill " +
                "@e[type=minecraft:armor_stand,tag=tdvEdit_visualizer]");
    }

    /**
     * Tries to visualize information on supported blocks (Command- and Noteblocks)
     * @param player Current player
     * @param location Current Location which should one around the player
     */
    @SuppressWarnings("ConstantConditions")
    private void tryVisualize(Player player, Location location){
        final BlockState wildState = location.getBlock().getState();

        // save booleans to use it later
        boolean isCb, isNb, isPlaysound = false;

        // if block is commandblock or noteblock
        if ((isCb = wildState instanceof CommandBlock) | (isNb = wildState.getBlockData().getMaterial() == Material.NOTE_BLOCK)){
            //calculate possible visualization armorstands
            final Collection<Entity> stands = location.getWorld().getNearbyEntities(new BoundingBox(location.getBlockX(), location.getBlockY(),
                            location.getBlockZ(),location.getBlockX() + 1.0, location.getBlockY() + 1.0,
                    location.getBlockZ() + 1.0), isPlaysoundShower);

            // if commandblock/noteblock has no visualization armorstand
            if (stands.isEmpty()){
                String data = "";

                // Build commandblock playsound data String
                if (isCb && (isPlaysound = CommandUtil.isCommandWithPlaysound(player, (CommandBlock)wildState))){
                    final HomogenTuple<Double> volpitch = CommandUtil.extractVolumePitch((CommandBlock)wildState);
                    final double volume = volpitch.getKey();
                    final double pitch = volpitch.getValue();
                    final String note = CommandUtil.getNoteFromPitch(pitch);

                    data = "" + ChatColor.LIGHT_PURPLE + volume + " " + ChatColor.GOLD +
                            pitch + ((note.equals("")) ? "" : ChatColor.AQUA + "/" + note);
                }

                // Build noteblock playsound data String
                if (isNb){
                    final int clicks = Integer.parseInt(CommandUtil.getBlockStates(wildState).get("note"));
                    final String note = CommandUtil.getNoteFromPitch(CommandUtil.getPitchFromClicks((clicks)));

                    data = "" + ChatColor.GOLD +
                            clicks + ((note.equals("")) ? "" : ChatColor.AQUA + "/" + note);
                }

                // Summon armorstand only if needed
                if (isNb || isCb && isPlaysound) {
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
                    stand.addScoreboardTag("tdvEdit_visualizer");
                    stand.setCustomName(data);
                }
            }
        }
    }
}
