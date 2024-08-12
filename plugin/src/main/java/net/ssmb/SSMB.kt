package net.ssmb

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import net.ssmb.blockwork.Blockwork
import net.ssmb.lifecycles.OnPluginDisable
import org.bukkit.scheduler.BukkitTask

class SSMB : SuspendingJavaPlugin() {
    private lateinit var blockworkTickTask: BukkitTask
    private val pluginDisableListeners = arrayListOf<OnPluginDisable>()

    override suspend fun onEnableAsync() {
        Blockwork.init {
            registerPlugin(this@SSMB)

            addPackage("net.ssmb.services")
            addPackage("net.ssmb.commands")
            addPackage("net.ssmb.components")

            container {
                register(this@SSMB)
                register(this@SSMB.logger)
            }
        }

        blockworkTickTask =
            server.scheduler.runTaskTimer(this, Runnable { Blockwork.doTick() }, 0L, 1L)

        Blockwork.modding.onListenerAdded<OnPluginDisable> { pluginDisableListeners.add(it) }

        Blockwork.modding.onListenerRemoved<OnPluginDisable> { pluginDisableListeners.remove(it) }

        logger.info("STARTED SSMB")
    }

    override suspend fun onDisableAsync() {
        blockworkTickTask.cancel()

        pluginDisableListeners.forEach { it.onPluginDisable() }

        logger.info("STOPPED SSMB")
    }
}
