package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.di.Injectable
import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.Twilight
import org.bukkit.entity.Player
import org.koin.core.component.inject

abstract class PassiveInstance(val definition: PassiveDefinition, val player: Player) : Manageable, Injectable {
    // Injected dependencies available to all passive instances
    protected val plugin: SuperSmashMobsBrawl by inject()
    protected val twilight: Twilight by inject()
    
    abstract override fun setup(): Unit

    abstract override fun teardown(): Unit
}
