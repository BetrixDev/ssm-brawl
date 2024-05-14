package net.ssmb.utils

import kotlin.random.Random

fun didRandomChanceHit(chance: Int): Boolean {
    return Random.nextInt(1, chance) == 1
}
