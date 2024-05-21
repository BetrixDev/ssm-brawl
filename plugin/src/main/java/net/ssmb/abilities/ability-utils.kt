package net.ssmb.abilities

import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.entity.Player

fun constructAbilityFromData(
    player: Player,
    abilityEntry: MinigameStartSuccess.PlayerData.KitData.AbilityEntry
): SsmbAbility {
    return when (abilityEntry.ability.id) {
        "sulphur_bomb" -> SulphurBombAbility(player, abilityEntry)
        "explode" -> ExplodeAbility(player, abilityEntry)
        "angry_herd" -> AngryHerdAbility(player, abilityEntry)
        "milk_spiral" -> MilkSpiralAbility(player, abilityEntry)
        else -> throw RuntimeException("No ability exists for id ${abilityEntry.ability.id}")
    }
}
