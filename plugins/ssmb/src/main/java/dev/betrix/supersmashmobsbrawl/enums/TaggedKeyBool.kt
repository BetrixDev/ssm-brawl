package dev.betrix.supersmashmobsbrawl.enums

enum class TaggedKeyBool(val id: String) {
    CAN_DOUBLE_JUMP("can_double_jump");

    companion object {
        fun fromId(id: String): TaggedKeyNum? {
            return TaggedKeyNum.values().find { it.id == id }
        }
    }
}