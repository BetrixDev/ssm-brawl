package net.ssmb.models

import org.bukkit.entity.Player

class RecordedMinigameAbilityUseEvent(
  override val dateRecorded: Long,
  val actor: Player,
  val abilityId: String,
  val damageDealt: Double?
) : RecordedMinigameEvent(dateRecorded)