package dev.betrix.superSmashMobsBrawl.kits

/**
 * Kit regeneration values based on original Mineplex Super Smash Mobs
 * 
 * Format: Kit Name -> Regeneration Rate (HP/s), Max Health (HP), Delay after damage (ticks)
 * 
 * Note: 1 HP = 0.5 hearts, 20 ticks = 1 second
 */
object KitValues {
    // High Health Kits (Tanks)
    object IRON_GOLEM {
        const val REGENERATION_RATE = 0.5 // 0.5 HP/s
        const val MAX_HEALTH = 30.0 // 15 hearts
        const val REGENERATION_DELAY = 120 // 6 seconds
        const val MELEE_DAMAGE = 8
    }
    
    object MAGMA_CUBE {
        const val REGENERATION_RATE = 0.4
        const val MAX_HEALTH = 26.0 // 13 hearts
        const val REGENERATION_DELAY = 100
        const val MELEE_DAMAGE = 7
    }
    
    // Medium Health Kits (Balanced)
    object CREEPER {
        const val REGENERATION_RATE = 0.5
        const val MAX_HEALTH = 18.0 // 9 hearts
        const val REGENERATION_DELAY = 100
        const val MELEE_DAMAGE = 6
    }
    
    object SKELETON {
        const val REGENERATION_RATE = 0.5
        const val MAX_HEALTH = 18.0 // 9 hearts
        const val REGENERATION_DELAY = 100
        const val MELEE_DAMAGE = 6
    }
    
    object ZOMBIE {
        const val REGENERATION_RATE = 0.5
        const val MAX_HEALTH = 20.0 // 10 hearts
        const val REGENERATION_DELAY = 100
        const val MELEE_DAMAGE = 7
    }
    
    // Low Health Kits (Glass Cannons)
    object BLAZE {
        const val REGENERATION_RATE = 0.6
        const val MAX_HEALTH = 16.0 // 8 hearts
        const val REGENERATION_DELAY = 80
        const val MELEE_DAMAGE = 5
    }
    
    object ENDERMAN {
        const val REGENERATION_RATE = 0.4
        const val MAX_HEALTH = 16.0 // 8 hearts
        const val REGENERATION_DELAY = 100
        const val MELEE_DAMAGE = 6
    }
    
    object SPIDER {
        const val REGENERATION_RATE = 0.6
        const val MAX_HEALTH = 14.0 // 7 hearts
        const val REGENERATION_DELAY = 80
        const val MELEE_DAMAGE = 5
    }
}