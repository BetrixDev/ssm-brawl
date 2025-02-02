package net.ssmb.abilities

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.get
import net.ssmb.extensions.setData
import net.ssmb.utils.TaggedKeyStr
import net.ssmb.utils.t
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

open class SsmbAbility(
    private val player: Player,
    private val abilityData: MinigameStartSuccess.PlayerData.KitData.AbilityEntry
) : Listener {
    private val plugin = SSMB.instance
    private val abilityId = abilityData.ability.id
    private val abilityCooldown = abilityData.ability.cooldown

    open var timeLastUsed = 0L

    open fun canUseAbility(): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime < timeLastUsed + abilityCooldown) {
            val timeLeft = ((timeLastUsed + abilityCooldown) - currentTime) / 1000.0
            player.sendMessage(
                t(
                    "ability.cooldownMessage",
                    mapOf(
                        "secondsLeft" to timeLeft.toString(),
                        "abilityName" to "ability.displayName.$abilityId"
                    )
                )
            )
            return false
        }

        return true
    }

    open fun initializeAbility() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        player.inventory.setItem(
            abilityData.abilityToolSlot,
            item(Material.IRON_SHOVEL) {
                displayName(t("ability.displayName.$abilityId"))
                persistentDataContainer.setData { set(TaggedKeyStr("ability_item_id"), abilityId) }
            }
        )
    }

    open fun destroyAbility() {
        HandlerList.unregisterAll(this)
        player.inventory.clear(abilityData.abilityToolSlot)
    }

    open fun doAbility() {
        println("No doAbility method found for ability $abilityId")
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val didCorrectActionHappen =
            event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK

        if (event.player != player || !didCorrectActionHappen) {
            return
        }

        val itemAbilityId =
            event.item?.itemMeta?.persistentDataContainer?.get(TaggedKeyStr("ability_item_id"))

        if (itemAbilityId != abilityId) {
            return
        }

        event.isCancelled = true

        if (canUseAbility()) {
            doAbility()
        }
    }

    fun getMetaDouble(key: String): Double? {
        return abilityData.ability.meta?.get(key)?.toDouble()
    }

    fun getMetaDouble(key: String, default: Double): Double {
        return getMetaDouble(key) ?: default
    }

    fun getMetaInt(key: String): Int? {
        return abilityData.ability.meta?.get(key)?.toInt()
    }

    fun getMetaInt(key: String, default: Int): Int {
        return getMetaInt(key) ?: default
    }
}
