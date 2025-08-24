package dev.betrix.superSmashMobsBrawl.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Material

/**
 * Kotlinx serialization deserializer for Paper Material using Registry API
 * Serializes/deserializes ItemType using NamespacedKey strings
 */
object MaterialSerializer : KSerializer<Material> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ItemType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Material) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Material {
        val name = decoder.decodeString().uppercase()

        return Material.getMaterial(name) ?: throw IllegalArgumentException("Material not found for key: $name")
    }
}