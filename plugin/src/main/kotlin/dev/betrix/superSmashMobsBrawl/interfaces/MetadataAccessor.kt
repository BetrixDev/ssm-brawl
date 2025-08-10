package dev.betrix.superSmashMobsBrawl.interfaces

interface MetadataAccessor {
    fun string(key: String): String?

    fun double(key: String): Double?

    fun int(key: String): Int?

    fun float(key: String): Float?

    fun long(key: String): Long?

    fun boolean(key: String): Boolean?
}
