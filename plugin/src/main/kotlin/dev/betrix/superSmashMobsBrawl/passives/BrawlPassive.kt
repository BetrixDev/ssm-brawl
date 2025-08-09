package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.services.DataService
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlPassive(val id: String, val player: Player) : Manageable(), KoinComponent {
    protected val dataService: DataService by inject()

    protected val passiveData =
        dataService.getPassive(id)
            ?: throw RuntimeException("No passive found in DataService with id $id")

    override fun setup() {
        super.setup()
        if (passiveData.userFacing) {
            player.sendDebugMessage("You have been given the $id passive")
        }
    }

    override fun teardown() {
        super.teardown()
        if (passiveData.userFacing) {
            player.sendDebugMessage("The $id passive has been removed")
        }
    }
}
