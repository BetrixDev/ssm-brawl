package net.ssmb.services

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import java.io.BufferedReader

data class MinigameDataRecord(val id: String, val teams: Int, val playersPerTeam: Int, val stocks: Int, val mapBlacklist: List<String>, val mapWhitelist: List<String>)

@Service
class MinigamesService(private val plugin: SSMB): OnStart {

    private val minigameData = hashMapOf<String, MinigameDataRecord>()

    override fun onStart() {
        plugin.launch {
            withContext(Dispatchers.IO) {
                val content = BufferedReader(plugin.getResource("minigames.json")!!.reader()).readText()

                val json = Json { ignoreUnknownKeys = true }
                val minigamesData = json.decodeFromString<List<MinigameDataRecord>>(content)

                minigamesData.forEach {
                    minigameData[it.id] = it
                }
            }
        }
    }

    fun getMinigameData(minigameId: String): MinigameDataRecord? {
        return minigameData[minigameId]
    }
}