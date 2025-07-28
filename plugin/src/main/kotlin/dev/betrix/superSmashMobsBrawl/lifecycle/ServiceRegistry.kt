package dev.betrix.superSmashMobsBrawl.lifecycle

/**
 * Registry for tracking all manageable services that need to be cleaned up during plugin shutdown.
 * 
 * This registry automatically calls teardown() on all registered services when teardownAll() is called,
 * ensuring proper cleanup during plugin disable.
 */
object ServiceRegistry {
    private val manageableServices = mutableListOf<Manageable>()
    private val asyncManageableServices = mutableListOf<AsyncManageable>()
    
    /**
     * Register a manageable service that should be torn down during plugin shutdown.
     */
    fun register(service: Manageable) {
        manageableServices.add(service)
    }
    
    /**
     * Register an async manageable service that should be torn down during plugin shutdown.
     */
    fun register(service: AsyncManageable) {
        asyncManageableServices.add(service)
    }
    
    /**
     * Tear down all registered manageable services.
     * This is typically called during plugin onDisable.
     */
    fun teardownAll() {
        manageableServices.forEach { service ->
            try {
                service.teardown()
            } catch (e: Exception) {
                // Log error but continue with other services
                System.err.println("Error during teardown of service ${service::class.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }
        manageableServices.clear()
    }
    
    /**
     * Asynchronously tear down all registered async manageable services.
     * This is typically called during plugin onDisableAsync.
     */
    suspend fun teardownAllAsync() {
        asyncManageableServices.forEach { service ->
            try {
                service.teardown()
            } catch (e: Exception) {
                // Log error but continue with other services
                System.err.println("Error during async teardown of service ${service::class.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }
        asyncManageableServices.clear()
    }
}