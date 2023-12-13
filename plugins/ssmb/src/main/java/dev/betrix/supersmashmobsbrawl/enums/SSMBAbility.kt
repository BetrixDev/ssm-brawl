package dev.betrix.supersmashmobsbrawl.enums

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl

private val plugin = SuperSmashMobsBrawl.instance

enum class SSMBAbility(val id: String, val cooldown: Int) {
    SULPHUR_BOMB("sulphur_bomb", 3),
    EXPLODE("explode", 7)
}