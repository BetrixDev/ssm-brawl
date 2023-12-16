package dev.betrix.supersmashmobsbrawl.enums

enum class TaggedKeyNum(val id: String) {
    ABILITY_CHARGE_TIME("ability_charge_time"),
    ABILITY_BASE_DAMAGE("ability_base_damage"),
    ABILITY_DAMAGE_MAX_RANGE("ability_damage_max_range"),
    ABILITY_ACTIVATION_PARTICLE_AMOUNT("ability_activation_particle_amount"),
    PROJECTILE_DAMAGE("projectile_damage"),
    PROJECTILE_DAMAGE_AOE("projectile_damage_range"),
    PROJECTILE_HITBOX_SIZE("projectile_hitbox_size"),
    PROJECTILE_KNOCKBACK_MULTIPLIER("projectile_knockback_multiplier"),
    TOOL_MELEE_DAMAGE("tool_melee_damage");

    companion object {
        fun fromId(id: String): TaggedKeyNum? {
            return values().find { it.id == id }
        }
    }
}