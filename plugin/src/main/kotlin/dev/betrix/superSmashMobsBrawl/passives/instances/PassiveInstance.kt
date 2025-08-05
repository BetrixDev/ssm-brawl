package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import dev.betrix.superSmashMobsBrawl.services.DataService

abstract class PassiveInstance(val definition: PassiveDefinition, val player: Player) :
    Manageable(), KoinComponent {
    protected val dataService: DataService by inject()

    override fun setup() {
        super.setup()

        player.sendDebugMessage("You have been given the ${definition.name} passive")
    }

    override fun teardown() {
        super.teardown()

        player.sendDebugMessage("The ${definition.name} passive has been removed")
    }
}
