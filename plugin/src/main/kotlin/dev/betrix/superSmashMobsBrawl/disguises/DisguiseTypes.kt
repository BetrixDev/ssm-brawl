package dev.betrix.superSmashMobsBrawl.disguises

import me.libraryaddict.disguise.disguisetypes.DisguiseType

fun resolveDisguiseType(vararg candidateNames: String, fallback: DisguiseType): DisguiseType {
    val candidates = candidateNames.map { it.uppercase() }.toSet()
    return DisguiseType.values().firstOrNull { candidates.contains(it.name.uppercase()) }
        ?: fallback
}
