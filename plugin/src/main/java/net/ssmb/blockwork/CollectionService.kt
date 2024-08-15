package net.ssmb.blockwork

import org.bukkit.World
import org.bukkit.entity.Entity

object CollectionService {
    private val taggedEntities = hashMapOf<Entity, ArrayList<String>>()
    private val taggedWorlds = hashMapOf<World, ArrayList<String>>()

    private val entityTaggedListeners = arrayListOf<(entity: Entity, tag: String) -> Unit>()
    private val entityUntaggedListeners = arrayListOf<(entity: Entity, tag: String) -> Unit>()
    private val worldTaggedListeners = arrayListOf<(world: World, tag: String) -> Unit>()
    private val worldUntaggedListeners = arrayListOf<(world: World, tag: String) -> Unit>()

    private val entityAddedListeners = hashMapOf<String, ArrayList<(entity: Entity) -> Unit>>()
    private val entityRemovedListeners = hashMapOf<String, ArrayList<(entity: Entity) -> Unit>>()
    private val worldAddedListeners = hashMapOf<String, ArrayList<(world: World) -> Unit>>()
    private val worldRemovedListeners = hashMapOf<String, ArrayList<(world: World) -> Unit>>()

    fun addTag(entity: Entity, tag: String) {
        var entry = taggedEntities[entity]

        if (entry == null) {
            entry = arrayListOf(tag)
            taggedEntities[entity] = entry
        } else if (entry.contains(tag)) {
            return
        }

        entry.add(tag)

        entityTaggedListeners.forEach { it.invoke(entity, tag) }
        entityAddedListeners[tag]?.forEach { it.invoke(entity) }
    }

    fun addTag(world: World, tag: String) {
        var entry = taggedWorlds[world]

        if (entry == null) {
            entry = arrayListOf(tag)
            taggedWorlds[world] = entry
        } else if (entry.contains(tag)) {
            return
        }

        entry.add(tag)

        worldTaggedListeners.forEach { it.invoke(world, tag) }
        worldAddedListeners[tag]?.forEach { it.invoke(world) }
    }

    fun removeTag(entity: Entity, tag: String) {
        val entry = taggedEntities[entity] ?: return

        val newEntry = entry.filter { it != tag }

        if (newEntry.isEmpty()) {
            taggedEntities.remove(entity)
        }

        taggedEntities[entity] = arrayListOf(*newEntry.toTypedArray())

        entityUntaggedListeners.forEach { it.invoke(entity, tag) }
        entityRemovedListeners[tag]?.forEach { it.invoke(entity) }
    }

    fun removeTag(world: World, tag: String) {
        val entry = taggedWorlds[world] ?: return

        val newEntry = entry.filter { it != tag }

        if (newEntry.isEmpty()) {
            taggedWorlds.remove(world)
        }

        taggedWorlds[world] = arrayListOf(*newEntry.toTypedArray())

        worldUntaggedListeners.forEach { it.invoke(world, tag) }
        worldRemovedListeners[tag]?.forEach { it.invoke(world) }
    }

    fun getTags(entity: Entity): ArrayList<String> {
        return taggedEntities[entity] ?: arrayListOf()
    }

    fun getTags(world: World): ArrayList<String> {
        return taggedWorlds[world] ?: arrayListOf()
    }

    fun getTaggedEntities(tag: String): List<Entity> {
        return taggedEntities.filter { it.value.contains(tag) }.map { it.key }
    }

    fun getTaggedWorlds(tag: String): List<World> {
        return taggedWorlds.filter { it.value.contains(tag) }.map { it.key }
    }

    fun hasTag(entity: Entity, tag: String): Boolean {
        val entry = taggedEntities[entity] ?: return false
        return entry.contains(tag)
    }

    fun hasTag(world: World, tag: String): Boolean {
        val entry = taggedWorlds[world] ?: return false
        return entry.contains(tag)
    }

    fun onEntityTagged(cb: (entity: Entity, tag: String) -> Unit): () -> Unit {
        entityTaggedListeners.add(cb)

        return { entityTaggedListeners.remove(cb) }
    }

    fun onEntityUntagged(cb: (entity: Entity, tag: String) -> Unit): () -> Unit {
        entityUntaggedListeners.add(cb)

        return { entityUntaggedListeners.remove(cb) }
    }

    fun onWorldTagged(cb: (world: World, tag: String) -> Unit): () -> Unit {
        worldTaggedListeners.add(cb)

        return { worldTaggedListeners.remove(cb) }
    }

    fun onWorldUntagged(cb: (world: World, tag: String) -> Unit): () -> Unit {
        worldUntaggedListeners.add(cb)

        return { worldUntaggedListeners.remove(cb) }
    }

    fun onEntityAdded(tag: String, cb: (entity: Entity) -> Unit): () -> Unit {
        val existingListeners = entityAddedListeners[tag]

        if (existingListeners == null) {
            entityAddedListeners[tag] = arrayListOf(cb)
        } else {
            existingListeners.add(cb)
        }

        return { existingListeners?.remove(cb) }
    }

    fun onEntityRemoved(tag: String, cb: (entity: Entity) -> Unit): () -> Unit {
        val existingListeners = entityRemovedListeners[tag]

        if (existingListeners == null) {
            entityRemovedListeners[tag] = arrayListOf(cb)
        } else {
            existingListeners.add(cb)
        }

        return { existingListeners?.remove(cb) }
    }

    fun onWorldAdded(tag: String, cb: (world: World) -> Unit): () -> Unit {
        val existingListeners = worldAddedListeners[tag]

        if (existingListeners == null) {
            worldAddedListeners[tag] = arrayListOf(cb)
        } else {
            existingListeners.add(cb)
        }

        return { existingListeners?.remove(cb) }
    }

    fun onWorldRemoved(tag: String, cb: (world: World) -> Unit): () -> Unit {
        val existingListeners = worldRemovedListeners[tag]

        if (existingListeners == null) {
            worldRemovedListeners[tag] = arrayListOf(cb)
        } else {
            existingListeners.add(cb)
        }

        return { existingListeners?.remove(cb) }
    }
}

fun Entity.getTags(): ArrayList<String> {
    return CollectionService.getTags(this)
}

fun Entity.addTag(tag: String) {
    CollectionService.addTag(this, tag)
}

fun Entity.removeTag(tag: String) {
    CollectionService.removeTag(this, tag)
}

fun Entity.hasTag(tag: String): Boolean {
    return CollectionService.hasTag(this, tag)
}

fun World.getTags(): ArrayList<String> {
    return CollectionService.getTags(this)
}

fun World.addTag(tag: String) {
    CollectionService.addTag(this, tag)
}

fun World.removeTag(tag: String) {
    CollectionService.removeTag(this, tag)
}

fun World.hasTag(tag: String): Boolean {
    return CollectionService.hasTag(this, tag)
}
