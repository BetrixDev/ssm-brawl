package net.ssmb.extensions

import net.ssmb.SSMB
import net.ssmb.utils.TaggedKeyBool
import net.ssmb.utils.TaggedKeyDouble
import net.ssmb.utils.TaggedKeyInt
import net.ssmb.utils.TaggedKeyStr
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PersistentDataContainerBuilder(private val container: PersistentDataContainer) {
    private val stringData = mutableMapOf<String, String>()
    private val intData = mutableMapOf<String, Int>()
    private val doubleData = mutableMapOf<String, Double>()
    private val boolData = mutableMapOf<String, Boolean>()

    fun set(key: TaggedKeyStr, value: String) {
        stringData[key.id] = value
    }

    fun set(key: TaggedKeyInt, value: Int) {
        intData[key.id] = value
    }

    fun set(key: TaggedKeyDouble, value: Double) {
        doubleData[key.id] = value
    }

    fun set(key: TaggedKeyBool, value: Boolean) {
        boolData[key.id] = value
    }

    fun build() {
        val plugin = SSMB.instance

        stringData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.STRING, it.value)
        }

        intData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.INTEGER, it.value)
        }

        doubleData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.DOUBLE, it.value)
        }

        boolData.forEach {
            container.set(NamespacedKey(plugin, it.key), PersistentDataType.BOOLEAN, it.value)
        }
    }
}

fun PersistentDataContainer.setData(builder: PersistentDataContainerBuilder.() -> Unit) {
    val persistentDataContainerBuilder = PersistentDataContainerBuilder(this)
    builder(persistentDataContainerBuilder)
    persistentDataContainerBuilder.build()
}

fun PersistentDataContainer.get(key: TaggedKeyStr): String? {
    return this.get(NamespacedKey(SSMB.instance, key.id), PersistentDataType.STRING)
}

fun PersistentDataContainer.get(key: TaggedKeyInt): Int? {
    return this.get(NamespacedKey(SSMB.instance, key.id), PersistentDataType.INTEGER)
}

fun PersistentDataContainer.get(key: TaggedKeyDouble): Double? {
    return this.get(NamespacedKey(SSMB.instance, key.id), PersistentDataType.DOUBLE)
}

fun PersistentDataContainer.get(key: TaggedKeyBool): Boolean? {
    return this.get(NamespacedKey(SSMB.instance, key.id), PersistentDataType.BOOLEAN)
}