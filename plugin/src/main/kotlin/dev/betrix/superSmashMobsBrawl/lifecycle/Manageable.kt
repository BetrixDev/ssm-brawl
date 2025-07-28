package dev.betrix.superSmashMobsBrawl.lifecycle

/**
 * Interface for objects that have a lifecycle with setup and teardown phases.
 * 
 * This interface provides a common contract for classes that need to:
 * - Initialize resources, listeners, runnables, or jobs during setup
 * - Clean up and cancel all tracked resources during teardown
 * 
 * Typical implementers include:
 * - Services that manage global state
 * - Ability instances that track TwilightRunnable and TwilightListener
 * - Kit instances that manage abilities and passives
 * - Minigame instances that need resource cleanup
 * - Passive instances that register event listeners
 * 
 * All implementers will have their teardown() method automatically called
 * during plugin shutdown via the plugin's onDisable method.
 */
interface Manageable {
    /**
     * Initialize any resources, listeners, runnables, or other components
     * that this object needs to function properly.
     */
    fun setup() {}
    
    /**
     * Clean up and cancel all tracked resources, listeners, runnables, jobs, etc.
     * This method should ensure that no resources are left hanging after the object
     * is no longer needed.
     */
    fun teardown()
}