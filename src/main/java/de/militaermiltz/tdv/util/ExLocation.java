package de.militaermiltz.tdv.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexander Ley
 * @version 1.2
 * This class contains extra functionality for Locations
 */
public class ExLocation extends Location {

    /**
     * Normal Loacation Constructor.
     */
    public ExLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    /**
     * Normal Loacation Constructor.
     */
    public ExLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    /**
     * Copies Location to create a new location.
     */
    public ExLocation(Location location){
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Creates a Location using a vector.
     */
    public ExLocation(@Nullable World world, Vector vector){
        this(world, vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     *  Creates a unique String which is used by HashMaps to find chestGuis and game instance by Location.
     */
    public static String getUniqueString(Location location){
        assert location.getWorld() != null;
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    /**
     * @return Returns if @param loc1 and @param loc2 are pointing to the same point int the world.
     */
    public static boolean equalsPos(Location loc1, Location loc2){
        if (loc1.getWorld() != loc2.getWorld()) return false;
        return loc1.toVector().equals(loc2.toVector());
    }

    /**
     * Floors the coordinates of @param location.
     */
    public static void align(Location location){
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
    }

    /**
     * Tests if @param testLoc intersects the cube which are made by @param startLoc and @param endLoc.
     * @param dir Relatively to Direction.
     */
    public static boolean intersects(Location startLoc, Location endLoc, Location testLoc, Direction dir){
        if (startLoc.getWorld() != endLoc.getWorld() || endLoc.getWorld() != testLoc.getWorld()) return false;

        //textLoc <= endLoc && startLoc <= testLoc
        return dir.getRelXTestPredicate(endLoc).test(testLoc.getX()) && dir.getRelYTestPredicate(endLoc).test(testLoc.getY())
                && dir.getRelZTestPredicate(endLoc).test(testLoc.getZ()) && (dir.getRelXTestPredicate(testLoc).test(startLoc.getX())
                && dir.getRelYTestPredicate(testLoc).test(startLoc.getY()) && dir.getRelZTestPredicate(testLoc).test(startLoc.getZ()));
    }

    /**
     * Align Location to x, y and z (Floors double coordinates).
     */
    public void align(){
        align(this);
    }

}
