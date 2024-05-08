package net.ssmb.utils

import java.io.File
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.ssmb.SSMB
import org.bukkit.configuration.file.YamlConfiguration

private val langConfig = YamlConfiguration()
private val variablePattern = Regex("\\{([^}]*)}")

fun initLang(plugin: SSMB) {
    val langFile = File(plugin.dataFolder, "lang.yml")
    langConfig.load(langFile)
}

private fun parseLangEntry(langKey: String, variables: HashMap<String, String>? = null): String {
    var parsedString = langConfig.getString(langKey) ?: langKey

    val variablesToReplace = variablePattern.findAll(langKey)

    variablesToReplace.forEach {
        val variableKey = it.value.substring(1, it.value.length - 1)

        if (variables != null && variables[variableKey] != null) {
            parsedString =
                parsedString.replace(it.value, variables.getOrDefault(variableKey, variableKey))
        } else if (langConfig.getString(variableKey) != null) {
            parsedString = parsedString.replace(it.value, parseLangEntry(variableKey, variables))
        }
    }

    return parsedString
}

fun t(key: String, variables: HashMap<String, String>? = null): Component {
    val parsedString = parseLangEntry(key, variables)

    return MiniMessage.miniMessage().deserialize(parsedString)
}
