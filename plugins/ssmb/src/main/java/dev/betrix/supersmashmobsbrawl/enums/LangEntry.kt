package dev.betrix.supersmashmobsbrawl.enums

enum class LangEntry(val id: String) {
    PREFIX("prefix"),
    QUEUE_ALREADY_JOINED("queue.already.joined"),
    QUEUE_JOINED("queue.joined"),
    QUEUE_LEFT("queue.left"),
    SERVER_PLAYER_LEFT("server.player.left"),
    SERVER_PLAYER_JOINED("server.player.joined"),
    ABILITY_COOLDOWN("ability.cooldown"),
    PASSIVE_HUNGER_CTA("passive.hunger.cta"),
    SIDEBAR_TITLE("sidebar.title"),
    MINIGAME_RESPAWNING("minigame.respawning"),
    HOLOGRAM_WELCOME_TITLE("hologram.welcome.title"),
    GUI_QUEUE_TITLE("gui.queue.title");

    companion object {
        fun fromId(id: String): LangEntry? {
            return values().find { it.id == id }
        }
    }
}