package dev.betrix.supersmashmobsbrawl.extensions

import org.bukkit.Particle

fun Particle.fromKey(key: String): Particle? {
    return Particle.values().find { it.key.value() == key }
}