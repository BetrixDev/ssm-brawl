package dev.betrix.superSmashMobsBrawl.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun mm(miniMessageString: String): Component {
    return MiniMessage.miniMessage().deserialize(miniMessageString)
}

val ONLY_PLAYERS_EXEC_MESSAGE = mm("<red>Only players can execute this command!</red>")
