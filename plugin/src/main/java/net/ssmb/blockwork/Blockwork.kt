package net.ssmb.blockwork

import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.blockwork.interfaces.OnTick
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections

class Blockwork {

    private val packagePaths = arrayListOf<String>()

    companion object {
        val container = Container()
        val modding = Modding()
        val components = ComponentManager()
        private val tickableInstances = arrayListOf<OnTick>()
        private var hasBlockworkInitialized = false
        private var lastTickTime = 0L
        internal lateinit var plugin: JavaPlugin

        fun init(init: Blockwork.() -> Unit): Blockwork {
            if (hasBlockworkInitialized) {
                throw Exception("Blockwork.init cannot be called more than once")
            }

            hasBlockworkInitialized = true

            val blockwork = Blockwork()

            blockwork.init()

            if (!::plugin.isInitialized) {
                throw Exception(
                    "Blockwork plugin not set, please use the registerPlugin method to set the plugin instance"
                )
            }

            Bukkit.getServer().pluginManager.registerEvents(components, plugin)

            blockwork.packagePaths.forEach {
                val reflections = Reflections(it)

                reflections.getTypesAnnotatedWith(Service::class.java).forEach { clazz ->
                    container.registerService(clazz)
                }

                reflections.getTypesAnnotatedWith(Component::class.java).forEach { clazz ->
                    components.registerComponent(clazz)
                }
            }

            container.instantiateServices()

            return blockwork
        }

        fun handleLifecycles(clazz: Class<*>, instance: Any) {
            val hasOnStart = OnStart::class.java.isAssignableFrom(clazz)
            val hasOnTick = OnTick::class.java.isAssignableFrom(clazz)

            if (hasOnStart) {
                (instance as OnStart).onStart()
            }

            if (hasOnTick) {
                tickableInstances.add(instance as OnTick)
            }
        }

        fun doTick() {
            val newLastTickTime = System.currentTimeMillis()
            val dt = if (lastTickTime == 0L) 0 else newLastTickTime - lastTickTime

            tickableInstances.forEach { it.onTick(dt) }
        }
    }

    fun container(init: Container.() -> Unit): Container {
        container.init()
        return container
    }

    fun addPackage(path: String) {
        packagePaths.add(path)
    }

    fun registerPlugin(plugin: JavaPlugin) {
        Blockwork.plugin = plugin
    }
}
