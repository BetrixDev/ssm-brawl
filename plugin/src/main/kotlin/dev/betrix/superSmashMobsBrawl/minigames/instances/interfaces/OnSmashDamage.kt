package dev.betrix.superSmashMobsBrawl.minigames.instances.interfaces

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent

interface OnSmashDamage {
    
    fun onSmashDamage(event: SmashDamageEvent): Boolean
    
    fun onPreSmashDamage(event: SmashDamageEvent): Double {
        return event.damage
    }
    
    fun onPostSmashDamage(event: SmashDamageEvent, finalDamage: Double) {
    }
}