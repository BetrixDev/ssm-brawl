package net.ssmb.services

import com.github.shynixn.mccoroutine.bukkit.launch
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.lifecycles.OnPlayerJoined
import net.ssmb.lifecycles.OnPluginDisable
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

data class PlayerDataRecord(val selectedKitId: String)

@Service
class PlayerDataService(private val plugin: SSMB, private val api: ApiService) :
    OnStart, OnPluginDisable, OnPlayerJoined {
    private val loadedDocuments = hashMapOf<Player, PlayerDataRecord>()

    private var autoSaveTask: BukkitTask? = null

    override fun onStart() {
        autoSaveTask =
            plugin.server.scheduler.runTaskTimer(plugin, Runnable { onAutoSave() }, 0L, 20 * 60 * 5)

        plugin.server.onlinePlayers.forEach { onPlayerJoined(it) }
    }

    override fun onPluginDisable() {
        autoSaveTask?.cancel()
    }

    override fun onPlayerJoined(player: Player) {
        plugin.launch {
            val playerDataDocument = api.fetchPlayerData(player)
            loadedDocuments[player] = playerDataDocument
        }
    }

    fun getPlayerData(player: Player): PlayerDataRecord {
        return loadedDocuments[player]!!
    }

    private fun onAutoSave() {
        loadedDocuments.forEach { (k, v) -> plugin.launch { api.savePlayerData(k, v) } }
    }
}
