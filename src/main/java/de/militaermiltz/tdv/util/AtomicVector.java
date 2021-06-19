package de.militaermiltz.tdv.util;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Ley
 * @version 1.0
 * This class represents a vector which is not mutable like the normal vector.
 * -> The AtomicVector cannot be changed by using operations.
 */
public class AtomicVector extends Vector {

    public AtomicVector() {
    }

    public AtomicVector(int x, int y, int z) {
        super(x, y, z);
    }

    public AtomicVector(double x, double y, double z) {
        super(x, y, z);
    }

    public AtomicVector(float x, float y, float z) {
        super(x, y, z);
    }

    public AtomicVector(Vector vector){
        super(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public @NotNull Vector add(@NotNull Vector vec) {
        return new AtomicVector(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }

    @Override
    public @NotNull Vector subtract(@NotNull Vector vec) {
        return new AtomicVector(x - vec.getX(), y - vec.getY(), z - vec.getZ());
    }

    @Override
    public @NotNull Vector multiply(@NotNull Vector vec) {
        return new AtomicVector(x * vec.getX(), y * vec.getY(), z * vec.getZ());
    }

    @Override
    public @NotNull Vector divide(@NotNull Vector vec) {
        return new AtomicVector(x / vec.getX(), y / vec.getY(), z / vec.getZ());
    }

    @Override
    public @NotNull Vector multiply(int m) {
        return new AtomicVector(x * m, y * m, z * m);
    }

    @Override
    public @NotNull Vector multiply(double m) {
        return new AtomicVector(x * m, y * m, z * m);
    }

    @Override
    public @NotNull Vector multiply(float m) {
        return new AtomicVector(x * m, y * m, z * m);
    }

    @Override
    public @NotNull Vector rotateAroundX(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double y = angleCos * getY() - angleSin * getZ();
        double z = angleSin * getY() + angleCos * getZ();
        return new AtomicVector(x, y, z);
    }

    @Override
    public @NotNull Vector rotateAroundY(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * getX() + angleSin * getZ();
        double z = -angleSin * getX() + angleCos * getZ();
        return new AtomicVector(x, y, z);
    }

    @Override
    public @NotNull Vector rotateAroundZ(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * getX() - angleSin * getY();
        double y = angleSin * getX() + angleCos * getY();
        return new AtomicVector(x, y, z);
    }

    @Override
    public @NotNull Vector rotateAroundNonUnitAxis(@NotNull Vector axis, double angle) throws IllegalArgumentException {
        double x = getX(), y = getY(), z = getZ();
        double x2 = axis.getX(), y2 = axis.getY(), z2 = axis.getZ();

        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        double dotProduct = this.dot(axis);

        double xPrime = x2 * dotProduct * (1d - cosTheta)
                + x * cosTheta
                + (-z2 * y + y2 * z) * sinTheta;
        double yPrime = y2 * dotProduct * (1d - cosTheta)
                + y * cosTheta
                + (z2 * x - x2 * z) * sinTheta;
        double zPrime = z2 * dotProduct * (1d - cosTheta)
                + z * cosTheta
                + (-y2 * x + x2 * y) * sinTheta;

        return new AtomicVector(xPrime, yPrime, zPrime);
    }
}
