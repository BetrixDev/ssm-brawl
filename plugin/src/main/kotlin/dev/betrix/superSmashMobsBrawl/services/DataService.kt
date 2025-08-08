package dev.betrix.superSmashMobsBrawl.services

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityDefFile
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.GameMapDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.HubMapDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDefFile
import dev.betrix.superSmashMobsBrawl.models.brawlData.MapDefFile
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDefFile
import dev.betrix.superSmashMobsBrawl.models.brawlData.PassiveDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.PassiveFileDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okio.buffer
import okio.source
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DataService : KoinComponent {
    private val plugin: SuperSmashMobsBrawl by inject()

    private val yaml =
        Yaml(
            serializersModule =
                SerializersModule {
                    polymorphic(MinigameDef::class) {
                        subclass(FfaMinigameDef::class)
                        subclass(TeamBasedStocksMinigameDef::class)
                    }
                },
            configuration =
                Yaml.default.configuration.copy(
                    polymorphismPropertyName = "type",
                    polymorphismStyle = PolymorphismStyle.Property,
                    strictMode = false,
                    decodeEnumCaseInsensitive = true,
                ),
        )

    private lateinit var kitDefs: Map<String, KitDef>
    private lateinit var gameMapDefs: Map<String, GameMapDef>
    private lateinit var hubMapDefs: Map<String, HubMapDef>
    private lateinit var passiveDefs: Map<String, PassiveDef>
    private lateinit var abilityDefs: Map<String, AbilityDef>
    private lateinit var minigameDefs: Map<String, MinigameDef>

    init {
        plugin.launch {
            withContext(Dispatchers.IO) {
                kitDefs = readFile<KitDefFile>("kits").kits.associateBy { it.id }
                passiveDefs = readFile<PassiveFileDef>("passives").passives.associateBy { it.id }
                abilityDefs = readFile<AbilityDefFile>("abilities").abilities.associateBy { it.id }
                minigameDefs =
                    readFile<MinigameDefFile>("minigames").minigames.associateBy { it.id }
                val mapDefs = readFile<MapDefFile>("maps")
                gameMapDefs = mapDefs.gameMaps.associateBy { it.id }
                hubMapDefs = mapDefs.hubMaps.associateBy { it.id }
            }
        }
    }

    fun getKit(id: String): KitDef? = if (::kitDefs.isInitialized) kitDefs[id] else null

    fun getGameMap(id: String): GameMapDef? =
        if (::gameMapDefs.isInitialized) gameMapDefs[id] else null

    fun getAllGameMaps(): List<GameMapDef> =
        if (::gameMapDefs.isInitialized) gameMapDefs.values.toList() else listOf()

    fun getHubMap(id: String): HubMapDef? = if (::hubMapDefs.isInitialized) hubMapDefs[id] else null

    fun getPassive(id: String): PassiveDef? =
        if (::passiveDefs.isInitialized) passiveDefs[id] else null

    fun getAbility(id: String): AbilityDef? =
        if (::abilityDefs.isInitialized) abilityDefs[id] else null

    fun getMinigame(id: String): MinigameDef? =
        if (::minigameDefs.isInitialized) minigameDefs[id] else null

    fun getAllMinigames(): List<MinigameDef> =
        if (::minigameDefs.isInitialized) minigameDefs.values.toList() else listOf()

    private suspend inline fun <reified T> readFile(path: String): T =
        withContext(Dispatchers.IO) {
            val resourcePath = "data/$path.yml"
            plugin.logger.info("Reading bundled data resource: $resourcePath")

            val inputStream =
                plugin.getResource(resourcePath)
                    ?: throw IllegalStateException("Bundled resource not found: $resourcePath")

            try {
                inputStream.source().buffer().use { source ->
                    val text = source.readUtf8()
                    yaml.decodeFromString<T>(text)
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to read/parse $resourcePath: ${e.message}")
                throw e
            }
        }
}
