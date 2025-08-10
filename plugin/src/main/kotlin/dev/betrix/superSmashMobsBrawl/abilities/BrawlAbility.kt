package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.extensions.getAs
import dev.betrix.superSmashMobsBrawl.interfaces.MetadataAccessor
import dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityUsage
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlAbility(val id: String, val player: Player) : Manageable(), KoinComponent {
    protected val plugin: JavaPlugin by inject()
    private val dataService: DataService by inject()
    protected val minigameService: MinigameService by inject()
    protected val kitService: KitService by inject()
    private val lang: LangService by inject()

    protected val abilityData =
        dataService.getAbility(id)
            ?: throw RuntimeException("No ability found in DataService with id $id")

    protected val minigameData = run {
        val minigameId = minigameService.getMinigameForPlayer(player)?.minigameId ?: return@run null

        return@run dataService.getMinigame(minigameId)
    }

    protected val kitData = run {
        val kitId = kitService.getKitForPlayer(player)?.id ?: return@run null

        return@run dataService.getKit(kitId)
    }

    private val abilityKey = NamespacedKey(plugin, "abilityId")

    private var lastUsed: Long = 0

    private var lastCheckForCanActivate = true

    protected lateinit var hotbarItemStack: ItemStack

    protected val metadata: MetadataAccessor = object : MetadataAccessor {
        override fun string(name: String): String? = getValue<String>(name)

        override fun double(name: String): Double? = getValue<Double>(name)

        override fun int(name: String): Int? = getValue<Int>(name)

        override fun float(name: String): Float? = getValue<Float>(name)

        override fun long(name: String): Long? = getValue<Long>(name)

        inline fun <reified T> getValue(name: String): T? {
            try {
                if (kitData != null) {
                    minigameData?.overrides?.kits?.get(kitData.id)?.abilities?.get(id)?.let {
                        it.metadata?.getAs<T>(name)?.let { value ->
                            return value
                        }
                    }

                    kitData.passives
                        .find { it.id == id }
                        ?.overrides
                        ?.metadata
                        ?.getAs<T>(name)
                        ?.let { value ->
                            return value
                        }
                }

                return abilityData.metadata.getAs<T>(name)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun setup() {
        val hotbarItemMaterial =
            Material.matchMaterial(abilityData.hotbarItem.uppercase())
                ?: run {
                    plugin.logger.warning(
                        "No material found with name ${abilityData.hotbarItem.uppercase()} reference by ability ${abilityData.id}"
                    )
                    Material.IRON_AXE
                }

        hotbarItemStack =
            ItemStack.of(hotbarItemMaterial).apply {
                amount = 1
                val meta = itemMeta

                meta.isUnbreakable = true
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                meta.customName(
                    lang.t("messages.abilities.hotbarItemTitle") { "abilityId" to abilityData.id }
                )
                meta.lore(
                    listOf(
                        lang.t("messages.abilities.hotbarItemDescription") {
                            "abilityId" to abilityData.id
                        }
                    )
                )
                meta.persistentDataContainer.set(abilityKey, PersistentDataType.STRING, id)

                itemMeta = meta
            }

        val hotBarItemSlot =
            when (player.inventory.getItem(abilityData.itemSlot)) {
                null -> abilityData.itemSlot
                else -> {
                    plugin.logger.warning(
                        "Item slot ${abilityData.itemSlot} was already taken, using first empty slot instead"
                    )
                    player.inventory.firstEmpty()
                }
            }

        player.inventory.setItem(hotBarItemSlot, hotbarItemStack)

        runnables.add(
            repeatingTask(10) {
                if (!lastCheckForCanActivate && canActivate()) {
                    lastCheckForCanActivate = true

                    player.sendMessage(
                        lang.t("messages.abilities.readyToActivate") {
                            "abilityId" to abilityData.id
                        }
                    )
                }
            }
        )

        listeners.add(
            event<PlayerInteractEvent>(player) {
                if (hand != EquipmentSlot.HAND) {
                    return@event
                }

                when (abilityData.usage) {
                    AbilityUsage.LEFT_CLICK -> {
                        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
                            return@event
                        }
                    }

                    AbilityUsage.RIGHT_CLICK -> {
                        if (
                            action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
                        ) {
                            return@event
                        }
                    }
                }

                val item = item ?: return@event
                val abilityId = getAbilityId(item) ?: return@event

                if (abilityId != id) {
                    return@event
                }

                isCancelled = true

                if (canActivate()) {
                    activate()
                }
            }
        )
    }

    override fun teardown() {
        if (::hotbarItemStack.isInitialized) {
            player.inventory.remove(hotbarItemStack)
        }

        listeners.forEach { it.unregister() }
        runnables.forEach { it.cancel() }
        jobs.forEach { it.cancel() }
    }

    open fun activate() {
        setCooldown()
    }

    open fun canActivate(): Boolean {
        if (isOnCooldown()) {
            val component =
                lang.t("messages.abilities.use.cooldown") {
                    "abilityId" to id
                    "seconds" to getRemainingCooldown()
                }
            player.sendMessage(component)
            return false
        }

        return true
    }

    protected fun isOnCooldown(): Boolean {
        val cooldownMs = abilityData.cooldown * 1000L
        return System.currentTimeMillis() - lastUsed < cooldownMs
    }

    protected fun getRemainingCooldown(): Int {
        val cooldownMs = (abilityData.cooldown * 1000).toLong()
        val elapsed = System.currentTimeMillis() - lastUsed
        return ((cooldownMs - elapsed) / 1000).coerceAtLeast(0).toInt()
    }

    protected fun setCooldown(time: Long = System.currentTimeMillis()) {
        lastCheckForCanActivate = false
        lastUsed = time
    }

    private fun getAbilityId(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null

        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(abilityKey, PersistentDataType.STRING)
    }
}
