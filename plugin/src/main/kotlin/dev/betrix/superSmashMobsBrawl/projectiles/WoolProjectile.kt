package dev.betrix.superSmashMobsBrawl.projectiles

import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WoolProjectile(
    owner: Player,
    private val onSolidify: (WoolProjectile) -> Unit
) : BrawlProjectile(owner, "wool_projectile") {

    var woolBlock: org.bukkit.block.Block? = null
    var originalBlockMaterial: Material? = null
    var originalBlockData: BlockData? = null

    init {
        enableBlockDetection(false)
        enableIdleDetection(true)
        velocityMultiplier = 0.7
    }

    override fun createProjectileEntity(): Entity {
        val item =
            owner.world.dropItem(
                owner.eyeLocation,
                ItemStack(Material.WHITE_WOOL),
            )
        item.pickupDelay = Int.MAX_VALUE
        return item
    }

    fun solidify(shouldArm: Boolean) {
        val entity = projectileEntity ?: return

        val location = entity.location.block.location

        val block = location.world.getBlockAt(location)

        if (!block.type.isAir && block.type.isSolid) {
            return
        }

        originalBlockMaterial = block.type
        originalBlockData = block.blockData.clone()

        block.type = Material.WHITE_WOOL

        woolBlock = block

        entity.remove()
        projectileEntity = null

        if (shouldArm) {
            onSolidify(this)
        }
    }

    override fun onIdle(): Boolean {
        solidify(true)
        return true
    }
}

