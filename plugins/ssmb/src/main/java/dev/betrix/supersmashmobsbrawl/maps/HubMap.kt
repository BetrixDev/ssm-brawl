package dev.betrix.supersmashmobsbrawl.maps

import dev.betrix.supersmashmobsbrawl.enums.WorldGeneratorType
import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import kotlinx.serialization.Serializable
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.SkinTrait
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class HubMap constructor(
    serverName: String,
) : SSMBMap(serverName, "main_hub", WorldGeneratorType.VOID) {

    private var podiumNpcs = arrayListOf<NPC>()
    private var holograms = arrayListOf<Hologram>()

    override fun afterPlayerTeleport(player: Player) {
        val actionItem = ItemStack(Material.COMPASS)
        val actionItemMeta = actionItem.itemMeta
        actionItemMeta.displayName(MiniMessage.miniMessage().deserialize("<aqua>Start a Game"))
        actionItem.itemMeta = actionItemMeta

        player.inventory.clear()
        player.inventory.setItem(4, actionItem)
        player.updateInventory()
        player.inventory.heldItemSlot = 4
    }

    private fun readHubMetadata(): HubMetadata {
        val metaJson = File("$cwd//worlds//main_hub//meta.json")

        return json.decodeFromString(metaJson.readText())
    }

    override fun createWorld() {
        super.createWorld()

        val hubMetaData = readHubMetadata()
        
        hubMetaData.holograms.forEach {
            val hologramLocation = Location(worldInstance, it.x, it.y, it.z)

            val hologram = DHAPI.createHologram(it.id, hologramLocation, it.lines)
            hologram.displayRange = it.displayRange
            hologram.updateInterval = it.updateIntervalTicks

            holograms.add(hologram)
        }
    }

    override fun destroyWorld() {
        podiumNpcs.forEach {
            it.destroy()
        }

        super.destroyWorld()
    }

    fun spawnNPCs() {
        val hubMetaData = readHubMetadata()

        val npcRegistry = CitizensAPI.getNPCRegistry()

        repeat(5) { index ->
            val npc = npcRegistry.createNPC(EntityType.PLAYER, index.toString())

            val spawnLocation = hubMetaData.podiumLocations[index]
            val npcLocation = Location(worldInstance, spawnLocation.x, spawnLocation.y, spawnLocation.z)
            npc.spawn(npcLocation)

            val skinTrait = npc.getTraitNullable(SkinTrait::class.java)
            skinTrait.skinName = "Airbays"

            podiumNpcs.add(npc)
        }
    }

}


@Serializable
data class HubMetadata(
    val worldBorderRadius: Double,
    val schematicRadius: Double,
    val schematicLowerLimit: Double,
    val schematicUpperLimit: Double,
    val spawnLocation: SpawnLocation,
    val schematicLocation: SpawnLocation,
    val podiumLocations: List<PodiumLocation>,
    val holograms: List<Hologram>
) {
    @Serializable
    data class SpawnLocation(
        val x: Double,
        val y: Double,
        val z: Double
    )

    @Serializable
    data class PodiumLocation(
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Double,
        val pitch: Double
    )

    @Serializable
    data class Hologram(
        val id: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val displayRange: Int,
        val updateIntervalTicks: Int,
        val lines: List<String>
    )
}