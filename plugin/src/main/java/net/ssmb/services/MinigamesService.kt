package net.ssmb.services

import com.github.shynixn.mccoroutine.bukkit.launch
import java.io.BufferedReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import org.bukkit.entity.Player

@Serializable
data class MinigameDataRecord(
    val id: String,
    val teams: Int,
    val playersPerTeam: Int,
    val stocks: Int,
    val mapBlacklist: List<String>,
    val mapWhitelist: List<String>
)

data class OngoingMinigameDataRecord(val gameId: String)

@Service
class MinigamesService(private val plugin: SSMB) : OnStart {
    private val minigameDataMap = hashMapOf<String, MinigameDataRecord>()
    private val ongoingMinigamesMap = hashMapOf<String, OngoingMinigameDataRecord>()

    override fun onStart() {
        plugin.launch {
            withContext(Dispatchers.IO) {
                val content =
                    BufferedReader(plugin.getResource("minigames.json")!!.reader()).readText()

                val json = Json { ignoreUnknownKeys = true }
                val minigamesData = json.decodeFromString<List<MinigameDataRecord>>(content)

                minigamesData.forEach { minigameDataMap[it.id] = it }
            }
        }
    }

    fun getMinigameData(minigameId: String): MinigameDataRecord? {
        return minigameDataMap[minigameId]
    }

    fun getOngoingMinigameData(gameId: String): OngoingMinigameDataRecord? {
        return ongoingMinigamesMap[gameId]
    }

    fun startNewMinigame(minigameId: String, players: List<Player>) {
        val minigameData = getMinigameData(minigameId) ?: throw Exception("No minigame found")
    }
}
