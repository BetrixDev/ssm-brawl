package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.BelowNameDisplay

class BelowNameDisplayUpdater(private val plugin: SuperSmashMobsBrawl = inject()): IteratingSystem(
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