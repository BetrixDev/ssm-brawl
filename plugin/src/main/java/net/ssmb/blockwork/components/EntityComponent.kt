package net.ssmb.blockwork.components

import org.bukkit.entity.Entity

open class EntityComponent<E : Entity> {
    lateinit var entity: E
}
