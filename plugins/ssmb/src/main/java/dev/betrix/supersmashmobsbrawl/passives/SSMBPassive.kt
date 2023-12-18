package dev.betrix.supersmashmobsbrawl.passives

import org.bukkit.event.Listener

abstract class SSMBPassive : Listener {
    open fun destroyPassive() {}
}