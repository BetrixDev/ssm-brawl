package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.BelowNameDisplayComponent

class BelowNameDisplaySystem(private val plugin: SuperSmashMobsBrawl = inject()) :
    IteratingSystem(family { all(BelowNameDisplayComponent) }) {
    private val lastNameDisplays = hashMapOf<BelowNameDisplayComponent, String>()

    override fun onTickEntity(entity: Entity) {
        val belowNameDisplay: BelowNameDisplayComponent = entity[BelowNameDisplayComponent]

        val last = lastNameDisplays[belowNameDisplay]

        if (last == null) {} else {}
    }
}
