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

    fun getComponent(key: String, variables: HashMap<String, String>? = null): Component {
        var rawString = langEntries[key] ?: return Component.text("NO ENTRY EXISTS FOR ID $key")

        val variablesToReplace = regex.findAll(rawString)

        variablesToReplace.forEach {
            rawString = if (langEntries[it.value] != null) {
                rawString.replace("{${it.value}}", langEntries[it.value]!!, ignoreCase = true)
            } else {
                val variable = variables!![it.value]!!
                rawString.replace("{${it.value}}", variable, ignoreCase = true)
            }
        }

        return MiniMessage.miniMessage().deserialize(rawString)
    }
}