package net.ssmb.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.ssmb.SSMB

private var langData = mapOf<String, String>()
private val variablePattern = Regex("\\{([^}]*)}")

suspend fun initLang(plugin: SSMB) {
    langData = plugin.api.langGetAllEntries()
}

private fun parseLangEntry(langKey: String, variables: Map<String, String>? = null): String {
    var parsedString = langData[langKey] ?: langKey

    val variablesToReplace = variablePattern.findAll(langKey)

    variablesToReplace.forEach {
        val variableKey = it.value.substring(1, it.value.length - 1)

        if (variables != null && variables[variableKey] != null) {
            parsedString =
                parsedString.replace(it.value, variables.getOrDefault(variableKey, variableKey))
        } else if (langData[variableKey] != null) {
            parsedString = parsedString.replace(it.value, parseLangEntry(variableKey, variables))
        }
    }

    return parsedString
}

fun t(key: String, variables: Map<String, String>? = null): Component {
    val parsedString = parseLangEntry(key, variables)

    return MiniMessage.miniMessage().deserialize(parsedString)
}
