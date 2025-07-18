package dev.betrix.superSmashMobsBrawl.kits

data class KitMetadata(
    val description: String = "",
    val type: KitType = KitType.DEFAULT,
    val meleeDamage: Int = 0,
    val passives: List<String>,
    val abilities: List<String>
)

enum class KitType { DEFAULT }

class Kit {
    var description = ""
    var type = KitType.DEFAULT
    var meleeDamage = 0
    val passives = arrayListOf<String>()
    val abilities = arrayListOf<String>()

    fun passive(id: String): Kit {
        passives.add(id)

        return this
    }

    fun ability(id: String): Kit {
        abilities.add(id)

        return this
    }

    internal fun build(): KitMetadata {
        require(meleeDamage != 0) { "Melee damage should not be 0" }

        return KitMetadata(
            description = description,
            type = type,
            meleeDamage = meleeDamage,
            passives = passives.toList(),
            abilities = abilities.toList(),
        )
    }
}

fun kit(block: Kit.() -> Unit): KitMetadata {
    return Kit().apply(block).build()
}