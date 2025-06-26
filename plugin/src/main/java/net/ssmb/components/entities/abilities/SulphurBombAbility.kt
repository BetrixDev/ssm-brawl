package net.ssmb.components.entities.abilities

import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.EntityComponent
import net.ssmb.blockwork.interfaces.OnDestroy
import net.ssmb.blockwork.interfaces.OnStart
import org.bukkit.entity.Player

@Component("ability_sulphur_bomb")
class SulphurBombAbility : EntityComponent<Player>(), OnStart, OnDestroy {
    override fun onStart() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }
}