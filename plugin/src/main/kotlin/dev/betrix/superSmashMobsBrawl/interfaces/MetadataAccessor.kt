package dev.betrix.superSmashMobsBrawl.interfaces

interface MetadataAccessor {
    fun string(name: String): String?

    fun double(name: String): Double?

    fun int(name: String): Int?

    fun float(name: String): Float?

    fun long(name: String): Long?
}
