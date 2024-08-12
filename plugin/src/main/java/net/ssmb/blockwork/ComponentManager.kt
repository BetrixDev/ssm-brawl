package net.ssmb.blockwork

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import kotlin.reflect.full.companionObjectInstance
import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.EntityComponent
import net.ssmb.blockwork.components.WorldComponent
import net.ssmb.blockwork.interfaces.AfterUnload
import net.ssmb.blockwork.interfaces.BeforeUnload
import net.ssmb.blockwork.interfaces.OnRemoveFromWorld
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

class ComponentManager : Listener {
    private val registeredWorldComponents = arrayListOf<Pair<String, Class<*>>>()
    private val registeredEntityComponents = arrayListOf<Pair<String, Class<*>>>()

    val constructedWorldComponents = arrayListOf<Pair<World, WorldComponent>>()
    val constructedEntityComponents = arrayListOf<Pair<Entity, EntityComponent<*>>>()

    val componentAddedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()
    val componentRemovedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()

    inline fun <reified T : Any> getAllWorldComponents(): List<T> {
        return constructedWorldComponents
            .filter { T::class.java.isAssignableFrom(it.second::class.java) }
            .map { it.second as T }
    }

    inline fun <reified T : Any> getComponentsInformed(world: World): List<T> {
        return constructedWorldComponents
            .filter { T::class.java.isAssignableFrom(it.second::class.java) && it.first == world }
            .map { it.second as T }
    }

    fun getComponents(world: World): List<WorldComponent> {
        return constructedWorldComponents.filter { it.first == world }.map { it.second }
    }

    inline fun <reified T : Any> getAllEntityComponents(): List<T> {
        return constructedEntityComponents
            .filter { T::class.java.isAssignableFrom(it.second::class.java) }
            .map { it.second as T }
    }

    inline fun <reified T : Any> getComponentsInformed(entity: Entity): List<T> {
        return constructedEntityComponents
            .filter { T::class.java.isAssignableFrom(it.second::class.java) && it.first == entity }
            .map { it.second as T }
    }

    fun getComponents(entity: Entity): List<EntityComponent<*>> {
        return constructedEntityComponents.filter { it.first == entity }.map { it.second }
    }

    inline fun <reified T : Any> onComponentAdded(noinline cb: (T) -> Unit): () -> Unit {
        @Suppress("UNCHECKED_CAST")
        componentAddedListeners.add(Pair(T::class.java, cb as (Any) -> Unit))

        // Returning a function that will remove the listener to avoid memory leaks
        return { componentAddedListeners.removeIf { it.first == T::class.java } }
    }

    inline fun <reified T : Any> onComponentRemoved(noinline cb: (T) -> Unit): () -> Unit {
        @Suppress("UNCHECKED_CAST")
        componentRemovedListeners.add(Pair(T::class.java, cb as (Any) -> Unit))

        // Returning a function that will remove the listener to avoid memory leaks
        return { componentRemovedListeners.removeIf { it.first == T::class.java } }
    }

    fun registerComponent(clazz: Class<*>) {
        val componentMetadata = clazz.getAnnotation(Component::class.java) ?: return

        if (WorldComponent::class.java.isAssignableFrom(clazz)) {
            registeredWorldComponents.add(Pair(componentMetadata.tag, clazz))
        } else if (EntityComponent::class.java.isAssignableFrom(clazz)) {
            registeredEntityComponents.add(Pair(componentMetadata.tag, clazz))
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        val tagMetadata = event.world.getMetadata(Tag.World.name)
        val tagString = if (tagMetadata.size > 0) (tagMetadata.first().asString()) else "default"

        val validComponents = getValidComponents(event.world, tagString, registeredWorldComponents)

        validComponents.map {
            val instance = Blockwork.container.constructDependency(it.second) as WorldComponent
            instance.world = event.world

            constructedWorldComponents.add(Pair(event.world, instance))
            handleComponentAdded(instance)
            Blockwork.modding.handleListenerAdded(instance)
            Blockwork.handleLifecycles(it.second, instance)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onWorldUnloadLowest(event: WorldUnloadEvent) {
        val attachedComponents = constructedWorldComponents.filter { it.first == event.world }

        attachedComponents.forEach {
            if (it.second is BeforeUnload) {
                (it.second as BeforeUnload).onBeforeUnload(event)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onWorldUnloadHighest(event: WorldUnloadEvent) {
        val attachedComponents = constructedWorldComponents.filter { it.first == event.world }

        if (!event.isCancelled) {
            attachedComponents.forEach {
                if (it.second is AfterUnload) {
                    (it.second as AfterUnload).onAfterUnload(event)
                }
            }
        }

        constructedWorldComponents.removeAll {
            val shouldRemove = it.first == event.world

            if (shouldRemove) {
                handleComponentRemoved(it.second)
                Blockwork.modding.handleListenerRemoved(it.second)
            }

            return@removeAll shouldRemove
        }
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val tagMetadata = event.entity.getMetadata(Tag.Entity.name)
        val tagString = if (tagMetadata.size > 0) (tagMetadata.first().asString()) else "default"

        val validComponents =
            getValidComponents(event.entity, tagString, registeredEntityComponents)

        validComponents.map {
            @Suppress("UNCHECKED_CAST")
            val instance =
                Blockwork.container.constructDependency(it.second) as EntityComponent<Entity>
            instance.entity = event.entity

            constructedEntityComponents.add(Pair(event.entity, instance))
            handleComponentAdded(instance)
            Blockwork.modding.handleListenerAdded(instance)
            Blockwork.handleLifecycles(it.second, instance)
        }
    }

    @EventHandler
    fun onEntityRemoveFromWorld(event: EntityRemoveFromWorldEvent) {
        val attachedComponents = constructedEntityComponents.filter { it.first == event.entity }

        attachedComponents.forEach {
            if (it.second is OnRemoveFromWorld) {
                (it.second as OnRemoveFromWorld).onRemoveFromWorld(event)
            }
        }

        constructedEntityComponents.removeAll {
            val shouldRemove = it.first == event.entity

            if (shouldRemove) {
                handleComponentRemoved(it.second)
                Blockwork.modding.handleListenerRemoved(it.second)
            }

            return@removeAll shouldRemove
        }
    }

    private fun handleComponentAdded(obj: Any) {
        componentAddedListeners.forEach { (inter, cb) ->
            if (inter.isAssignableFrom(obj::class.java)) {
                cb(obj)
            }
        }
    }

    private fun handleComponentRemoved(obj: Any) {
        componentRemovedListeners.forEach { (inter, cb) ->
            if (inter.isAssignableFrom(obj::class.java)) {
                cb(obj)
            }
        }
    }

    private fun <T : Any> getValidComponents(
        target: T,
        tag: String,
        components: List<Pair<String, Class<*>>>
    ): List<Pair<String, Class<*>>> {
        return components
            .filter { it.first == tag }
            .filter { (_, clazz) ->
                val companion = clazz.kotlin.companionObjectInstance ?: return@filter true

                val predicate =
                    companion::class.members.find { m -> m.name == "predicate" }
                        ?: return@filter true

                return@filter predicate.call(companion, target) as Boolean
            }
    }
}
