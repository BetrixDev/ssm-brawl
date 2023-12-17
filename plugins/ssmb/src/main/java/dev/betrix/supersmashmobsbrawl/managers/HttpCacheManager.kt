package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.utils.md5
import kotlin.collections.set

class HttpCacheManager {

    private val cache = hashMapOf<String, CacheEntry>()

    fun set(
        url: String,
        body: String? = null,
        response: String
    ) {
        val hash = md5("$url$body")
        cache[hash] = CacheEntry(System.currentTimeMillis(), response)
    }

    fun get(
        url: String,
        body: String? = null
    ): String? {
        val hash = md5("$url$body")
        val cacheEntry = cache[hash] ?: return null

        return cacheEntry.response
    }

    fun invalidate(
        url: String,
        body: String,
    ) {
        val hash = md5("$url$body")
        cache.remove(hash)
    }
}

data class CacheEntry(val dateAdded: Long, val response: String)