package dev.betrix.supersmashmobsbrawl.extensions

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PersistentDataContainerBuilder(private val container: PersistentDataContainer) {
    private val stringData = mutableMapOf<String, String>()
    private val doubleData = mutableMapOf<String, Double>()

    fun set(key: TaggedKeyNum, value: Double) {
        doubleData[key.id] = value
    }

    fun set(key: TaggedKeyStr, value: String) {
        stringData[key.id] = value
    }

    fun build() {
        val plugin = SuperSmashMobsBrawl.instance

        stringData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.STRING, it.value)
        }

        doubleData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.DOUBLE, it.value)
        }
    }
}

fun PersistentDataContainer.setData(builder: PersistentDataContainerBuilder.() -> Unit) {
    val persistentDataContainerBuilder = PersistentDataContainerBuilder(this)
    builder(persistentDataContainerBuilder)
    persistentDataContainerBuilder.build()
}

fun PersistentDataContainer.get(key: TaggedKeyNum): Double? {
    return this.get(NamespacedKey(SuperSmashMobsBrawl.instance, key.id), PersistentDataType.DOUBLE)
}

fun PersistentDataContainer.get(key: TaggedKeyStr): String? {
    return this.get(NamespacedKey(SuperSmashMobsBrawl.instance, key.id), PersistentDataType.STRING)
}

fun PersistentDataContainer.has(key: TaggedKeyNum): Boolean {
    return this.has(NamespacedKey(SuperSmashMobsBrawl.instance, key.id))
}

fun PersistentDataContainer.has(key: TaggedKeyStr): Boolean {
    return this.has(NamespacedKey(SuperSmashMobsBrawl.instance, key.id))
}