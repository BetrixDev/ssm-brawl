package dev.betrix.superSmashMobsBrawl.serializers

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.NamespacedKey
import org.bukkit.Sound

/**
 * Kotlinx serialization deserializer for Bukkit Sound enum Serializes/deserializes Sound as string
 * key names
 */
object SoundSerializer : KSerializer<Sound> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Sound", PrimitiveKind.STRING)

    private val soundRegistry by lazy {
        RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT)
    }

    override fun serialize(encoder: Encoder, value: Sound) {
        val key =
            soundRegistry.getKey(value)
                ?: throw IllegalStateException("Sound not found in registry: $value")
        encoder.encodeString(key.toString())
    }

    override fun deserialize(decoder: Decoder): Sound {
        val keyString = decoder.decodeString()
        val namespacedKey =
            try {
                if (keyString.contains(':')) {
                    NamespacedKey.fromString(keyString)
                } else {
                    NamespacedKey.minecraft(keyString)
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid sound key format: $keyString", e)
            }

        return namespacedKey?.let { soundRegistry.get(it) }
            ?: throw IllegalArgumentException("Sound not found for key: $keyString")
    }
}
