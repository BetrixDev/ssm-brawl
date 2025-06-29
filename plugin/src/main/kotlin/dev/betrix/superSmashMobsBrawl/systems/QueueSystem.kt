package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import dev.betrix.superSmashMobsBrawl.components.InQueue
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayer

class QueueSystem : IntervalSystem() {

    private val playerQueueFamily = world.family { all(MinecraftPlayer, InQueue) }

    override fun onTick() {
        val queueMap = hashMapOf<String, ArrayList<Entity>>()

        playerQueueFamily.forEach { entity ->
            val queueId = entity[InQueue].queueId

            if (!queueMap.containsKey(queueId)) {
                queueMap[queueId] = arrayListOf(entity)
            } else {
                queueMap[queueId]?.add(entity)
            }
        }
    }
}
