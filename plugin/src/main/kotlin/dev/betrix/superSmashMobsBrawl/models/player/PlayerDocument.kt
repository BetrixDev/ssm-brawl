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
        @Serializable(with = InstantIsoSerializer::class) val firstJoinedDate: Instant,
        @Serializable(with = InstantIsoSerializer::class) val lastJoinedDate: Instant,
        val isFirstTimeOnServer: Boolean,
        val username: String,
        val uuid: String,
        var selectedKitId: String,
        val banData: BanData?
)

@Serializable(with = BannedBySerializer::class)
sealed class BannedBy {
        data object System : BannedBy()

        data class Moderator(val uuid: String) : BannedBy()
}

@Serializable
data class BanData
@OptIn(ExperimentalTime::class)
constructor(
        val isBanned: Boolean,
        val reason: String,
        @Serializable(with = NullableInstantIsoSerializer::class) val expiresAt: Instant?,
        @Serializable(with = InstantIsoSerializer::class) val bannedAt: Instant,
        val bannedBy: BannedBy
)
