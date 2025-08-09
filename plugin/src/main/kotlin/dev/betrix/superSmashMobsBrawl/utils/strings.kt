package dev.betrix.superSmashMobsBrawl.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun mm(miniMessageString: String): Component {
    return MiniMessage.miniMessage().deserialize(miniMessageString)
}