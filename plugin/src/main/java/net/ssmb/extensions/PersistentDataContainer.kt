package net.ssmb.extensions

import net.ssmb.SSMB
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PersistentDataContainerBuilder(private val container: PersistentDataContainer) {
    private val stringData = mutableMapOf<String, String>()

    fun set(key: String, value: String) {
        stringData[key] = value
    }

    fun build() {
        val plugin = SSMB.instance

        stringData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.STRING, it.value)
        }
    }
}

fun PersistentDataContainer.setData(builder: PersistentDataContainerBuilder.() -> Unit) {
    val persistentDataContainerBuilder = PersistentDataContainerBuilder(this)
    builder(persistentDataContainerBuilder)
    persistentDataContainerBuilder.build()
}

fun PersistentDataContainer.get(key: String): String? {
    return this.get(NamespacedKey(SSMB.instance, key), PersistentDataType.STRING)
}

fun PersistentDataContainer.has(key: String): Boolean {
    return this.has(NamespacedKey(SSMB.instance, key))
}