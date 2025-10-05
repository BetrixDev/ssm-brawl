package dev.betrix.superSmashMobsBrawl.models.player

import dev.betrix.superSmashMobsBrawl.serializers.BannedBySerializer
import dev.betrix.superSmashMobsBrawl.serializers.InstantIsoSerializer
import dev.betrix.superSmashMobsBrawl.serializers.NullableInstantIsoSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDocument
@OptIn(ExperimentalTime::class)
constructor(
    val isFirstTimeOnServer: Boolean,
    @Serializable(with = InstantIsoSerializer::class) val lastJoinDate: Instant,
    val headSkinBase64: String?,
    var banData: List<PlayerBanData>?,
    var selectedKitId: String,
)

@Serializable(with = BannedBySerializer::class)
sealed class BannedBy {
    data object System : BannedBy()

    data class Moderator(val uuid: String) : BannedBy()
}

@Serializable
data class PlayerBanData
@OptIn(ExperimentalTime::class)
constructor(
    val isBanned: Boolean,
    val reason: String,
    @Serializable(with = NullableInstantIsoSerializer::class) val expiresAt: Instant?,
    val bannedAt: String,
    val bannedBy: BannedBy,
)
