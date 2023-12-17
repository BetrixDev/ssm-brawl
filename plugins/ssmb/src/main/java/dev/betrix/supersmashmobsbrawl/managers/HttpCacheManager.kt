package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.utils.md5
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

class HttpCacheManager {

    private val cache = hashMapOf<String, CacheEntry>()

    fun set(
        url: String,
        headers: HashMap<String, String>,
        body: String,
        response: String
    ) {
        val hash = md5("$url$body$headers")
        cache[hash] = CacheEntry(System.currentTimeMillis(), response)
    }

    fun <T> get(
        url: String,
        headers: HashMap<String, String>,
        body: String,
        deserializer: DeserializationStrategy<T>
    ): T? {
        val hash = md5("$url$body$headers")
        val cacheEntry = cache[hash] ?: return null

        return Json.decodeFromString(deserializer, cacheEntry.response)
    }

    fun invalidate(
        url: String,
        body: String,
        headers: HashMap<String, String>
    ) {
        val hash = md5("$url$body$headers")
        cache.remove(hash)
    }

    suspend fun <T, V> revalidate(
        url: String,
        headers: HashMap<String, String>,
        body: V,
        serializer: SerializationStrategy<V>,
        deserializer: DeserializationStrategy<T>
    ): T? {
        val hash = md5("$url$body$headers")

        val newResponse = SuperSmashMobsBrawl.instance.api.client.post(url) {
            headers {
                headers.forEach {
                    append(it.key, it.value)
                }
            }
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(serializer, body))
        }

        val responseString = newResponse.bodyAsText()

        cache[hash] = CacheEntry(System.currentTimeMillis(), responseString)
        return Json.decodeFromString(deserializer, responseString)
    }
}

data class CacheEntry(val dateAdded: Long, val response: String)