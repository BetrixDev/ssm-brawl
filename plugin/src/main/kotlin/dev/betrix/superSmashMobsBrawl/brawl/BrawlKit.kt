package dev.betrix.superSmashMobsBrawl.brawl

import dev.betrix.superSmashMobsBrawl.disguises.Disguise
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import org.bukkit.entity.Player

open class BrawlKit(val id: String, val player: Player) {
    val abilities = arrayListOf<BrawlAbility>()
    val passives = arrayListOf<BrawlPassive>()
    protected var disguise: Disguise? = null

    fun applyDisguise(disguise: Disguise) {
        this.disguise = disguise
    }

    open fun setup() {
        abilities.forEach { ability -> ability.setup() }

        passives.forEach { passive -> passive.setup() }

        HotbarService.setupHotbarItems(this)

        disguise?.setup()

        player.sendDebugMessage("You have been given the ${id} kit")
    }

    open fun teardown() {
        HotbarService.clearHotbarItems(player)

        disguise?.teardown()

        abilities.forEach { it.teardown() }
        abilities.clear()

        passives.forEach { it.teardown() }
        passives.clear()

        player.sendDebugMessage("The ${id} kit has been removed")
    }
}
