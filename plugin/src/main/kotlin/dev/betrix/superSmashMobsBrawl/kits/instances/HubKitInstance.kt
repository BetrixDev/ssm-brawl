package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import org.bukkit.entity.Player

class HubKitInstance(definition: KitDefinition, player: Player) :
    KitInstance(definition, player) {
    
    override fun setup() {
        // Set up passives but skip hotbar setup since this is a hub-only kit
        definition.metadata.passives.forEach {
            val passiveInstance = it.createInstance(player)
            passiveInstances.add(passiveInstance)
            passiveInstance.setup()
        }
        
        // Don't set up abilities or hotbar items for hub kit
        // This keeps the hub clean without unnecessary UI elements
    }
    
    override fun teardown() {
        // Clear passives
        passiveInstances.forEach {
            it.teardown()
            passiveInstances.remove(it)
        }
        
        // No hotbar items to clear for hub kit
    }
}