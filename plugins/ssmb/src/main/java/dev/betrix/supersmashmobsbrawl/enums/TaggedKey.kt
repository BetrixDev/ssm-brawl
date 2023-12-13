package dev.betrix.supersmashmobsbrawl.enums

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import org.bukkit.NamespacedKey

enum class TaggedKey(val key: NamespacedKey) {
    /** String */
    ABILITY_ITEM_ID(createKey("ability_item_id")),

    /** Double */
    PROJECTILE_DAMAGE(createKey("projectile_damage")),

    /** Double */
    PROJECTILE_DAMAGE_RANGE(createKey("projectile_damage_range")),

    /** String */
    PROJECTILE_THROWER_UUID(createKey("projectile_thrower_uuid")),

    /** String */
    PROJECTILE_EXPLOSION_PARTICLE(createKey("projectile_explosion_particle")),

    /** String */
    PROJECT_EXPLOSION_SOUND(createKey("project_explosion_sound")),

    /** Double */
    TOOL_MELEE_DAMAGE(createKey("tool_melee_damage"))
}

private val plugin = SuperSmashMobsBrawl.instance

private fun createKey(key: String): NamespacedKey {
    return NamespacedKey(plugin, key)
}
