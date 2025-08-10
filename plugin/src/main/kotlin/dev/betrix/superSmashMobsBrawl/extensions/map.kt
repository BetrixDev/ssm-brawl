package dev.betrix.superSmashMobsBrawl.extensions

import com.charleskorn.kaml.YamlScalar

inline fun <reified T> Map<String, YamlScalar>?.getAs(name: String): T? {
    val scalar = this?.get(name) ?: return null
    val value = scalar.content.trim()

    return when (T::class) {
        String::class -> value as T
        Double::class -> value.toDoubleOrNull() as T?
        Int::class -> value.toIntOrNull() as T?
        Float::class -> value.toFloatOrNull() as T?
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}
