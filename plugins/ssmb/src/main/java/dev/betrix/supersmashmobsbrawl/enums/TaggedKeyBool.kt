package dev.betrix.supersmashmobsbrawl.enums

enum class TaggedKeyBool(val id: String) {
    PLAYER_CAN_DOUBLE_JUMP("can_double_jump"),
    PLAYER_IS_EXPLODE_ACTIVE("player_is_explode_active");

    companion object {
        fun fromId(id: String): TaggedKeyNum? {
            return TaggedKeyNum.values().find { it.id == id }
        }
    }
}