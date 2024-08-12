package net.ssmb.blockwork.interfaces

import org.bukkit.event.world.WorldUnloadEvent

interface BeforeUnload {
    fun onBeforeUnload(event: WorldUnloadEvent)
}
