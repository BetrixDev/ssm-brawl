package net.ssmb.services

import net.ssmb.SSMB
import net.ssmb.blockwork.Blockwork
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.lifecycles.OnServerLoad
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

@Service(loadOrder = -1)
class ServerLoadService(private val plugin: SSMB) : OnStart, Listener {
    private val listeners = arrayListOf<OnServerLoad>()

    override fun onStart() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        Blockwork.modding.onListenerAdded<OnServerLoad> {
            println("Added listener for OnSsmbEnabled ${it::class.simpleName}")
            listeners.add(it)
        }

        Blockwork.modding.onListenerRemoved<OnServerLoad> { listeners.remove(it) }
    }

    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        println("Server loaded!")
        listeners.forEach { it.onServerLoad() }
    }
}
