package net.ssmb.services

import net.ssmb.SSMB
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class LangService(private val plugin: SSMB) {
    private val locales = hashMapOf<String, YamlConfiguration>()

    init {
        val localesDir = File("${plugin.dataFolder}${File.separator}lang")

        localesDir.listFiles()?.forEach {
            val yaml = YamlConfiguration()
            yaml.load(it)
            locales[it.name.substringBefore(".")] = yaml
        }
    }

    fun getComponent(locale: String, key: String, variables: HashMap<String, String>) {
        val rawString = locales[locale]?.getString(key)!!

    }
}