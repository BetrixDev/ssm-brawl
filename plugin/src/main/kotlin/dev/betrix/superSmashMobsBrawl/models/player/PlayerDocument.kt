package dev.betrix.superSmashMobsBrawl.models.player

import dev.betrix.superSmashMobsBrawl.serializers.BannedBySerializer
import dev.betrix.superSmashMobsBrawl.serializers.InstantIsoSerializer
import dev.betrix.superSmashMobsBrawl.serializers.NullableInstantIsoSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

@Serializable
data class PlayerDocument
@OptIn(ExperimentalTime::class)
constructor(
    val isFirstTimeOnServer: Boolean,
    @Serializable(with = InstantIsoSerializer::class) val lastJoinDate: Instant,
    val headSkinBase64: String?,
    val stats: MutableMap<String, JsonElement>,
    val banData: PlayerBanData?,
) {
    /**
     * Type-safe getter for stats values
     *
     * @return The value of the stat if it exists and can be cast to the specified type, null
     *   otherwise
     */
    inline fun <reified T> getStat(key: String): T? {
        val element = stats[key] as? JsonPrimitive ?: return null

        return when (T::class) {
            String::class -> element.contentOrNull as? T
            Double::class -> element.doubleOrNull as? T
            Int::class -> element.intOrNull as? T
            Long::class -> element.longOrNull as? T
            Float::class -> element.floatOrNull as? T
            Boolean::class -> element.booleanOrNull as? T
            else -> null
        }
    }

    /**
     * Type-safe getter for stats values with a default value if the stat does not exist. If the
     * stat does not exist, it will be created with the default value. **Mutates the stats map if
     * the stat does not exist.**
     *
     * @return The value of the stat if it exists and can be cast to the specified type, otherwise
     *   the default value
     */
    inline fun <reified T> getStat(key: String, default: T): T {
        val value = getStat<T>(key)

        if (value == null) {
            setStat(key, default)
        }

        return value ?: default
    }

    /** Type-safe setter for stats values. Mutates the stats map */
    inline fun <reified T> setStat(key: String, value: T) {
        val jsonElement =
            when (value) {
                is String -> JsonPrimitive(value)
                is Number -> JsonPrimitive(value)
                is Boolean -> JsonPrimitive(value)
                else ->
                    throw IllegalArgumentException(
                        "Stats only support String, Number, or Boolean types"
                    )
            }

        stats[key] = jsonElement
    }

    /** Remove a stat from the stats map. */
    fun removeStat(key: String) {
        stats.remove(key)
    }
}

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
