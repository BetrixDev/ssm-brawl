package net.ssmb.blockwork.interfaces

import org.bukkit.event.world.WorldUnloadEvent

interface AfterUnload {
    fun onAfterUnload(event: WorldUnloadEvent)
}
