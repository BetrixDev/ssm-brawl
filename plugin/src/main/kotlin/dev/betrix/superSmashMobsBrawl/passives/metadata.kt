package dev.betrix.superSmashMobsBrawl.passives

data class PassiveMetadata(val description: String = "", val userFacing: Boolean = false)

class PassiveBuilder {
    var description = ""
    var userFacing = false

    internal fun build(): PassiveMetadata {
        return PassiveMetadata(description = description, userFacing = userFacing)
    }
}

fun passive(block: PassiveBuilder.() -> Unit): PassiveMetadata {
    return PassiveBuilder().apply(block).build()
}
