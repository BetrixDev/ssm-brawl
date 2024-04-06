package net.ssmb.models

class RecordedMinigameAbilityUseEvent(
    override val dateRecorded: Long,
    val abilityId: String,
    val damageDealt: Double?
) : RecordedMinigameEvent(dateRecorded)