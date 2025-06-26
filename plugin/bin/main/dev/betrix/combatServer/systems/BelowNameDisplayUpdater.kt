package dev.betrix.combatServer.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.combatServer.CombatServer
import dev.betrix.combatServer.components.BelowNameDisplay

class BelowNameDisplayUpdater(private val plugin: CombatServer = inject()): IteratingSystem(
    family { all(BelowNameDisplay) }
) {
    private val lastNameDisplays = hashMapOf<BelowNameDisplay, String>()

    override fun onTickEntity(entity: Entity) {
        val belowNameDisplay: BelowNameDisplay = entity[BelowNameDisplay]

        val last = lastNameDisplays[belowNameDisplay]

        if (last == null) {

        } else {
            
        }
    }
}