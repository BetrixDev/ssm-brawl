package net.ssmb.services

import net.ssmb.blockwork.annotations.Service

@Service
class QueueService(private val api: ApiService) {
    val defaultMinigameQueues = arrayOf("test", "ranked")
}
