package dev.betrix.supersmashmobsbrawl.enums

enum class TaggedKeyStr(val id: String) {
    ABILITY_ITEM_ID("ability_item_id"),
    PROJECTILE_THROWER_UUID("projectile_thrower_uuid"),
    PROJECTILE_EXPLOSION_PARTICLE("projectile_explosion_particle"),
    PROJECTILE_EXPLOSION_SOUND("projectile_explosion_sound");

    companion object {
        fun fromId(id: String): TaggedKeyStr? {
            return TaggedKeyStr.values().find { it.id == id }
        }
    }
}