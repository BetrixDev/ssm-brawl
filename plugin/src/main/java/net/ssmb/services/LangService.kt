package net.ssmb.services

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.ssmb.SSMB

class LangService {
    private val plugin = SSMB.instance
    private val api = plugin.api
    private lateinit var langEntries: Map<String, String>
    private val regex = Regex("\\{([^}]*)}")

    suspend fun initLangService() {
        langEntries = api.langAllEntries()
    }

    private fun parseLang(rawString: String, variables: HashMap<String, String>? = null): String {
        var parsedString = rawString

        val variablesToReplace = regex.findAll(rawString)

        variablesToReplace.forEach {
            parsedString =
                if (langEntries[it.value] != null) {
                    parsedString.replace(
                        "{${it.value}}",
                        parseLang(langEntries[it.value]!!, variables),
                        ignoreCase = true
                    )
                } else {
                    val variable = variables!![it.value]!!
                    parsedString.replace("{${it.value}}", variable, ignoreCase = true)
                }
        }

        return parsedString
    }

    fun getComponent(key: String, variables: HashMap<String, String>? = null): Component {
        val rawString = langEntries[key] ?: "NO ENTRY EXISTS FOR KEY $key"

        val parsedString = parseLang(rawString, variables)

        return MiniMessage.miniMessage().deserialize(parsedString)
    }
}
