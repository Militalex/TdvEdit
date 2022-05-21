package de.militaermiltz.tdv.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Alexander Ley
 * @version 1.1
 * This Class is the Bukkit equivalent for an object with a scheduled task timer.
 */
public abstract class BukkitTickable {
    protected BukkitTask task;
    private final Plugin plugin;
    private boolean started = false;

    /**
     * Constructor
     * @param plugin is needed to use Bukkit.getScheduler.runTaskTimer(plugin, ...)
     */
    protected BukkitTickable(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the Timer
     * @param delay Start delay in ticks.
     * @param period Timer period in ticks.
     */
    public void start(long delay, long period){
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, delay, period);
        started = true;
    }

    /**
     * @return Returns if timer was started or not.
     */
    public boolean getStarted(){
        return started;
    }

    /**
     * Tick Loop
     */
    public abstract void tick();

    /**
     * Stops the Timer and makes it ready for next time.
     */
    public void stop() {
        try {
            task.cancel();
        }
        catch (Exception ignored) { }
    }
}
