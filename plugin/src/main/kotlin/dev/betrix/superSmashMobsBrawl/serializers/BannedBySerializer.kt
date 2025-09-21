package dev.betrix.superSmashMobsBrawl.serializers

import dev.betrix.superSmashMobsBrawl.models.player.BannedBy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BannedBySerializer : KSerializer<BannedBy> {
    override val descriptor = PrimitiveSerialDescriptor("BannedBy", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BannedBy) {
        when (value) {
            is BannedBy.System -> encoder.encodeString("system")
            is BannedBy.Moderator -> encoder.encodeString(value.uuid)
        }
    }

    override fun deserialize(decoder: Decoder): BannedBy {
        val string = decoder.decodeString()
        return if (string == "system") {
            BannedBy.System
        } else {
            BannedBy.Moderator(string)
        }
    }
}