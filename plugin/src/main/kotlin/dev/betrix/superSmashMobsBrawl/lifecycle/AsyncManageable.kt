package dev.betrix.superSmashMobsBrawl.lifecycle

/**
 * Interface for objects that have a lifecycle with setup and teardown phases,
 * where teardown operations need to be executed asynchronously.
 * 
 * This interface is specifically for classes that need to perform async operations
 * during teardown, such as unloading worlds or cleaning up coroutines.
 * 
 * All implementers will have their teardown() method automatically called
 * during plugin shutdown via the plugin's onDisable method.
 */
interface AsyncManageable {
    /**
     * Initialize any resources, listeners, runnables, or other components
     * that this object needs to function properly.
     */
    fun setup() {}
    
    /**
     * Asynchronously clean up and cancel all tracked resources, listeners, runnables, jobs, etc.
     * This method should ensure that no resources are left hanging after the object
     * is no longer needed.
     */
    suspend fun teardown()
}