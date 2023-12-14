package dev.betrix.supersmashmobsbrawl.enums

enum class TaggedKeyNum(val id: String) {
    PROJECTILE_DAMAGE("projectile_damage"),
    PROJECTILE_DAMAGE_RANGE("projectile_damage_range"),
    TOOL_MELEE_DAMAGE("tool_melee_damage");

    companion object {
        fun fromId(id: String): TaggedKeyNum? {
            return values().find { it.id == id }
        }
    }
}