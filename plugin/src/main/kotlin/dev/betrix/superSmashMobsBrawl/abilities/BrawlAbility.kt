package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.extensions.getAs
import dev.betrix.superSmashMobsBrawl.extensions.isSword
import dev.betrix.superSmashMobsBrawl.interfaces.MetadataAccessor
import dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityUsage
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import gg.flyte.twilight.scheduler.repeatingTask
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.BlocksAttacks
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlAbility(val id: String, val player: Player) : Manageable(), KoinComponent {
    protected val plugin: SuperSmashMobsBrawl by inject()
    private val dataService: DataService by inject()
    protected val minigameService: MinigameService by inject()
    protected val kitService: KitService by inject()
    protected val lang: LangService by inject()

    protected val abilityData by lazy {
        dataService.getAbility(id)
            ?: throw RuntimeException("No ability found in DataService with id $id")
    }

    protected val minigameData by lazy {
        val minigameId =
            minigameService.getMinigameForPlayer(player)?.minigameId ?: return@lazy null

        return@lazy dataService.getMinigame(minigameId)
    }

    protected val kitData by lazy {
        val kitId = kitService.getKitForPlayer(player)?.id ?: return@lazy null

        return@lazy dataService.getKit(kitId)
    }

    private val abilityKey = NamespacedKey(plugin, "abilityId")

    private var lastUsed: Long = 0

    private var lastCheckForCanActivate = true

    protected lateinit var hotbarItemStack: ItemStack
        private set

    protected var latestInteractEvent: PlayerInteractEvent? = null
        private set

    protected val elapsedSinceLastActivation: Long
        get() = System.currentTimeMillis() - lastUsed

    protected val metadata: MetadataAccessor =
        object : MetadataAccessor {
            override fun string(key: String): String? = getValue<String>(key)

            override fun double(key: String): Double? = getValue<Double>(key)

            override fun int(key: String): Int? = getValue<Int>(key)

            override fun float(key: String): Float? = getValue<Float>(key)

            override fun long(key: String): Long? = getValue<Long>(key)

            override fun boolean(key: String): Boolean? = getValue<Boolean>(key)

            inline fun <reified T> getValue(name: String): T? {
                try {
                    if (kitData != null) {
                        minigameData?.overrides?.kits?.get(kitData?.id)?.abilities?.get(id)?.let {
                            it.metadata?.getAs<T>(name)?.let { value ->
                                return value
                            }
                        }

                        kitData
                            ?.abilities
                            ?.find { it.id == id }
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
                        "No material '${abilityData.hotbarItem}' for ability ${abilityData.id}. Defaulting to IRON_AXE."
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

                if (type.isSword) {
                    setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks().build())
                }
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
            repeatingTask(5) {
                if (!lastCheckForCanActivate && canActivate(false)) {
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
                if (hand != null && hand != EquipmentSlot.HAND) return@event

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

                //                isCancelled = true

                if (canActivate()) {
                    latestInteractEvent = this
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
        // Avoid client-side item cooldown visuals affecting combat rhythm; we keep only our timers
        try {
            player.setCooldown(hotbarItemStack, 0)
        } catch (_: Throwable) {
        }
        player.sendMessage(lang.t("messages.abilities.use.success") { "abilityId" to id })
    }

    open fun canActivate(sendMessage: Boolean = true): Boolean {
        if (isOnCooldown()) {
            if (sendMessage) {
                val component =
                    lang.t("messages.abilities.use.cooldown") {
                        "abilityId" to id
                        "seconds" to getRemainingCooldown()
                    }
                player.sendMessage(component)
            }
            return false
        }

        return true
    }

    protected fun isOnCooldown(): Boolean {
        val cooldownMs = abilityData.cooldown * 1000L
        return elapsedSinceLastActivation < cooldownMs
    }

    protected fun getRemainingCooldown(): Int {
        val cooldownMs = (abilityData.cooldown * 1000).toLong()
        return ((cooldownMs - elapsedSinceLastActivation) / 1000).coerceAtLeast(0).toInt()
    }

    private fun getRemainingCooldownTicks(): Int {
        val cooldownMs = (abilityData.cooldown * 1000).toLong()
        val remainingMs = (cooldownMs - elapsedSinceLastActivation).coerceAtLeast(0)
        return (remainingMs / 50).toInt() // 1 tick = 50 ms
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
