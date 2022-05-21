package de.militaermiltz.tdv.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Alexander Ley
 * @version 1.4
 * This enum handles different Directions and offers Methods to do ^ ^ ^ things (Relative to direction).
 */
public enum Direction {
    SOUTH(new AtomicVector(1, 0, 0), new AtomicVector(0, 1, 0), new AtomicVector(0, 0, 1)),
    NORTH(new AtomicVector(-1, 0, 0), new AtomicVector(0, 1, 0), new AtomicVector(0, 0, -1)),
    WEST(new AtomicVector(0, 0, 1), new AtomicVector(0, 1, 0), new AtomicVector(-1, 0, 0)),
    EAST(new AtomicVector(0, 0, -1), new AtomicVector(0, 1, 0), new AtomicVector(1, 0, 0));

    //Relative direction vectors.
    private final Vector relVecX, relVecY, relVecZ;

    Direction(Vector relVecX, Vector relVecY, Vector relVecZ){
        this.relVecX = relVecX;
        this.relVecY = relVecY;
        this.relVecZ = relVecZ;
    }

    @Override
    public String toString() {
        final String old = super.toString();
        return "" + old.charAt(0) + old.substring(1).toLowerCase();
    }

    /**
     * Parse Direction from Location.
     */
    public static Direction getFromLocation(Location location){
        return getFromYaw(location.getYaw());
    }

    /**
     * Parse Direction from Yaw.
     */
    public static Direction getFromYaw(float yaw){
        double direction = (yaw % 360);
        if (direction < 0) direction += 360;

        if ((direction < 45 && direction >= 0) || direction >= 315) return SOUTH;
        else if (direction >= 45 && direction < 135) return WEST;
        else if (direction >= 135 && direction < 225) return NORTH;
        else return EAST;
    }

    /**
     * Parse Direction from Vector.
     */
    public static Direction getFromVector(Vector vector){
        final Vector align = new Vector(0, 0, 1);
        return getFromYaw((float) Math.toDegrees(align.angle(vector)));
    }

    /**
     * Parse Direction from player Location.
     */
    public static Direction getFromPlayer(Player player){
        return getFromLocation(player.getLocation());
    }

    /**
     * Get the yaw in degrees the direction is pointing to.
     */
    public float getYaw(){
        return switch (this) {
            case NORTH -> 180;
            case EAST -> 270;
            case SOUTH -> 0;
            default -> 90;
        };
    }

    /**
     * @return Returns opposite Direction.
     */
    public Direction getOpposite(){
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            default -> EAST;
        };
    }

    /**
     * @return Returns Direction 90 degrees rotated.
     */
    public Direction rotate90(){
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            default -> NORTH;
        };
    }

    /**
     * @return Returns Direction 180 degrees rotated.
     */
    public Direction rotate180(){
        return rotate90().rotate90();
    }

    /**
     * @return Returns Direction 270 degrees rotated.
     */
    public Direction rotate270(){
        return rotate180().rotate90();
    }

    /**
     * @return Returns  if XZ coordinates are swapped -> Only by West and East
     */
    public boolean XZswaped(){
        return this == EAST || this == WEST;
    }

    /**
     * @return Returns if Direction is pointing negative.
     */
    public boolean isPointingNegative(){
        return this == WEST || this == NORTH;
    }

    /**
     * @return Returns relative x Vector.
     */
    public Vector getRelVecX() {
        return new Vector(relVecX.getBlockX(), relVecX.getBlockY(), relVecX.getBlockZ());
    }

    /**
     * @return Returns relative y Vector.
     */
    public Vector getRelVecY() {
        return new Vector(relVecY.getBlockX(), relVecY.getBlockY(), relVecY.getBlockZ());
    }

    /**
     * @return Returns relative z Vector.
     */
    public Vector getRelVecZ() {
        return new Vector(relVecZ.getBlockX(), relVecZ.getBlockY(), relVecZ.getBlockZ());
    }

    /**
     * @return Returns relative posX test predicate.
     * This predicate tests if a coordinate is smaller or larger than location.getBlockX() (Depends on Direction).
     * This Predicate is optimised to use in a for loop.
     */
    public Predicate<Double> getRelXTestPredicate(Location location){
        return switch (this) {
            case EAST, WEST -> integer -> (this.getRelVecX().getBlockZ() < 0) ? integer < location.getBlockX() : integer > location.getBlockX();
            default -> integer -> (this.getRelVecX().getBlockX() >= 0) ? integer < location.getBlockX() : integer > location.getBlockX();
        };
    }

    /**
     * @return Returns relative posY test predicate.
     * This predicate tests if a coordinate is smaller than location.getBlockY().
     * This Predicate is optimised to use in a for loop.
     */
    public Predicate<Double> getRelYTestPredicate(Location location){
        return integer -> (this.getRelVecY().getBlockY() >= 0) ? integer < location.getBlockY() : integer > location.getBlockY();
    }

    /**
     * @return Returns relative posZ test predicate.
     * This predicate tests if a coordinate is smaller or larger than location.getBlockZ() (Depends on Direction).
     * This Predicate is optimised to use in a for loop.
     */
    public Predicate<Double> getRelZTestPredicate(Location location){
        return switch (this) {
            case EAST, WEST -> integer -> (this.getRelVecZ().getBlockX() < 0) ? integer < location.getBlockZ() : integer > location.getBlockZ();
            default -> integer -> (this.getRelVecZ().getBlockZ() >= 0) ? integer < location.getBlockZ() : integer > location.getBlockZ();
        };
    }

    /**
     * @return Returns a function which can increase in relative posX.
     * (^operand ^ ^)
     */
    public Function<Double, Double> increaseInRelX(double operand){
        return switch (this) {
            case EAST, WEST -> pos -> (this.getRelVecX().getBlockZ() < 0) ? pos += operand : (pos -= operand);
            default -> pos -> (this.getRelVecX().getBlockX() >= 0) ? pos += operand : (pos -= operand);
        };
    }

    /**
     * @return Returns a function which can increase in relative posY.
     * (^ ^operand ^)
     */
    public Function<Double, Double> increaseInRelY(double operand){
        return pos -> (this.getRelVecY().getBlockY() >= 0) ? pos += operand : (pos -= operand);
    }

    /**
     * @return Returns a function which can increase in relative posZ.
     * (^ ^ ^operand)
     */
    public Function<Double, Double> increaseInRelZ(double operand){
        return switch (this) {
            case EAST, WEST -> pos -> (this.getRelVecZ().getBlockX() < 0) ? pos += operand : (pos -= operand);
            default -> pos -> (this.getRelVecZ().getBlockZ() >= 0) ? pos += operand : (pos -= operand);
        };
    }

    /**
     * @return Returns a function which can increment in relative posX.
     * (^1 ^ ^)
     */
    public Function<Double, Double> incrementInRelX(){
        return increaseInRelX(1);
    }

    /**
     * @return Returns a function which can increment in relative posY.
     * (^ ^1 ^)
     */
    public Function<Double, Double> incrementInRelY(){
        return increaseInRelY(1);
    }

    /**
     * @return Returns a function which can increment in relative posZ.
     * (^ ^ ^1)
     */
    public Function<Double, Double> incrementInRelZ(){
        return increaseInRelZ(1);
    }

    /**
     * @return Returns a function which can decrement in relative posX.
     * (^-1 ^ ^)
     */
    public Function<Double, Double> decrementInRelX(){
        return increaseInRelX(-1);
    }

    /**
     * @return Returns a function which can decrement in relative posY.
     * (^ ^-1 ^)
     */
    public Function<Double, Double> decrementInRelY(){
        return increaseInRelY(-1);
    }

    /**
     * @return Returns a function which can decrement in relative posZ.
     * (^ ^ ^-1)
     */
    public Function<Double, Double> decrementInRelZ(){
        return increaseInRelZ(-1);
    }

    /**
     * @return Returns a function adding to a location a vector which is relative to direction.
     */
    public BiFunction<Location, Vector ,Location> addRelative(){
        return (location, vector) -> {

            if (XZswaped()) {
                vector = new Vector(vector.getZ(), vector.getY(), vector.getX());
            }
            return new Location(location.getWorld(),
                         increaseInRelX(vector.getX()).apply(location.getX()),
                         increaseInRelY(vector.getY()).apply(location.getY()),
                         increaseInRelZ(vector.getZ()).apply(location.getZ())
            );
        };
    }

    /**
     * @return Returns a function subtracting to a location a vector which is relative to direction.
     */
    public BiFunction<Location, Vector ,Location> subtractRelative(){
        return (location, vector) -> {
            if (XZswaped()) {
                vector = new Vector(vector.getZ(), vector.getY(), vector.getX());
            }
            return new Location(location.getWorld(),
                         increaseInRelX(-vector.getX()).apply(location.getX()),
                         increaseInRelY(-vector.getY()).apply(location.getY()),
                         increaseInRelZ(-vector.getZ()).apply(location.getZ())
            );
        };
    }

    /**
     * Loop from startLoc to endLoc.
     * @param action The action which is done with every Position.
     */
    public void loopFromTo(Location startLoc, Location endLoc, Consumer<Location> action){
        for (double x = startLoc.getBlockX(); getRelXTestPredicate(endLoc).test(x); x = incrementInRelX().apply(x)){
            for (double y = startLoc.getBlockY(); getRelYTestPredicate(endLoc).test(y); y = incrementInRelY().apply(y)){
                for (double z = startLoc.getBlockZ(); getRelZTestPredicate(endLoc).test(z); z = incrementInRelZ().apply(z)){
                    action.accept(new Location(startLoc.getWorld(), x, y, z));
                }
            }
        }
    }
}
