package dev.betrix.supersmashmobsbrawl.extensions

import dev.betrix.supersmashmobsbrawl.enums.TaggedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

fun PersistentDataContainer.setValues(builder: PersistentDataContainer.() -> Unit) {
    builder()
}

fun PersistentDataContainer.setString(key: TaggedKey, value: String) {
    set(key.key, PersistentDataType.STRING, value)
}

fun PersistentDataContainer.getString(key: TaggedKey): String? {
    return get(key.key, PersistentDataType.STRING)
}

fun PersistentDataContainer.setDouble(key: TaggedKey, value: Double) {
    set(key.key, PersistentDataType.DOUBLE, value)
}

fun PersistentDataContainer.getDouble(key: TaggedKey): Double? {
    return get(key.key, PersistentDataType.DOUBLE)
}

fun PersistentDataContainer.setInt(key: TaggedKey, value: Int) {
    set(key.key, PersistentDataType.INTEGER, value)
}

fun PersistentDataContainer.getInt(key: TaggedKey): Int? {
    return get(key.key, PersistentDataType.INTEGER)
}
