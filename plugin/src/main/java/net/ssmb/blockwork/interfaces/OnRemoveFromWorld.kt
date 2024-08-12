package net.ssmb.blockwork.interfaces

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent

interface OnRemoveFromWorld {
    fun onRemoveFromWorld(event: EntityRemoveFromWorldEvent)
}
