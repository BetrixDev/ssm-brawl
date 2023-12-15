package dev.betrix.supersmashmobsbrawl.extensions

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyBool
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import org.bukkit.entity.Entity
import org.bukkit.metadata.FixedMetadataValue

class EntityMetadataBuilder(private val entity: Entity) {
    private val metadata = mutableMapOf<String, Any>()

    fun set(metadataKey: TaggedKeyNum, value: Double) {
        metadata[metadataKey.id] = value
    }

    fun set(metadataKey: TaggedKeyStr, value: String) {
        metadata[metadataKey.id] = value
    }

    fun set(metadataKey: TaggedKeyBool, value: Boolean) {
        metadata[metadataKey.id] = value
    }

    fun build() {
        val plugin = SuperSmashMobsBrawl.instance

        metadata.forEach {
            entity.setMetadata(it.key, FixedMetadataValue(plugin, it.value))
        }
    }
}

fun Entity.setMetadata(builder: EntityMetadataBuilder.() -> Unit) {
    val metadataBuilder = EntityMetadataBuilder(this)
    builder(metadataBuilder)
    metadataBuilder.build()
}

fun Entity.getMetadata(metadataKey: TaggedKeyNum): Double? {
    return this.getMetadata(metadataKey.id).getOrNull(0)?.value() as Double?
}

fun Entity.getMetadata(metadataKey: TaggedKeyStr): String? {
    val value = this.getMetadata(metadataKey.id).getOrNull(0) ?: return null
    return value.value().toString()
}

fun Entity.getMetadata(metadataKey: TaggedKeyBool): Boolean? {
    val value = this.getMetadata(metadataKey.id).getOrNull(0) ?: return null
    return value.value().toString().toBoolean()
}