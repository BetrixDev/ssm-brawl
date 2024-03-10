package net.ssmb.services

import net.ssmb.SSMB
import net.ssmb.kits.CreeperKit
import net.ssmb.kits.IKit

class KitService(private val plugin: SSMB) {
    private val kits = hashMapOf<String, IKit>(
        "creeper" to CreeperKit(plugin)
    )

    fun kitById(kitId: String): IKit {
        return kits[kitId]!!
    }
}