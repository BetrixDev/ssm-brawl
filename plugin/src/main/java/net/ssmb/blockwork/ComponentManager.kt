package net.ssmb.blockwork

import kotlin.reflect.full.companionObjectInstance
import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.EntityComponent
import net.ssmb.blockwork.components.WorldComponent
import net.ssmb.blockwork.interfaces.OnDestroy
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.event.Listener

class ComponentManager : Listener {
    private val registeredWorldComponents = arrayListOf<Pair<String, Class<*>>>()
    private val registeredEntityComponents = arrayListOf<Pair<String, Class<*>>>()

    val constructedWorldComponents = arrayListOf<Pair<World, WorldComponent>>()
    val constructedEntityComponents = arrayListOf<Pair<Entity, EntityComponent<*>>>()

    val componentAddedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()
    val componentRemovedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()

    init {
        CollectionService.onEntityTagged { entity, tag ->
            val validComponents = getValidComponents(entity, tag, registeredEntityComponents)

            validComponents.map {
                @Suppress("UNCHECKED_CAST")
                val instance =
                    Blockwork.container.constructDependency(it.second) as EntityComponent<Entity>
                instance.entity = entity
                instance.tag = tag

                constructedEntityComponents.add(Pair(entity, instance))
                handleComponentAdded(instance)
                Blockwork.modding.handleListenerAdded(instance)
                Blockwork.handleLifecycles(it.second, instance)
            }
        }

        CollectionService.onEntityUntagged { entity, tag ->
            constructedEntityComponents.removeAll {
                val shouldRemove = it.first == entity && it.second.tag == tag

                if (shouldRemove) {
                    handleComponentRemoved(it.second)
                    Blockwork.modding.handleListenerRemoved(it.second)
                    (it.second as? OnDestroy)?.onDestroy()
                }

                return@removeAll shouldRemove
            }
        }

        CollectionService.onWorldTagged { world, tag ->
            val validComponents = getValidComponents(world, tag, registeredWorldComponents)

            validComponents.map {
                val instance = Blockwork.container.constructDependency(it.second) as WorldComponent
                instance.world = world
                instance.tag = tag

                constructedWorldComponents.add(Pair(world, instance))
                handleComponentAdded(instance)
                Blockwork.modding.handleListenerAdded(instance)
                Blockwork.handleLifecycles(it.second, instance)
            }
        }

        CollectionService.onWorldUntagged { world, tag ->
            constructedWorldComponents.removeAll {
                val shouldRemove = it.first == world && it.second.tag == tag

                if (shouldRemove) {
                    handleComponentRemoved(it.second)
                    Blockwork.modding.handleListenerRemoved(it.second)
                    (it.second as? OnDestroy)?.onDestroy()
                }

                return@removeAll shouldRemove
            }
        }
    }

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
        return { componentAddedListeners.removeAll { it.first == T::class.java } }
    }

    inline fun <reified T : Any> onComponentRemoved(noinline cb: (T) -> Unit): () -> Unit {
        @Suppress("UNCHECKED_CAST")
        componentRemovedListeners.add(Pair(T::class.java, cb as (Any) -> Unit))

        // Returning a function that will remove the listener to avoid memory leaks
        return { componentRemovedListeners.removeAll { it.first == T::class.java } }
    }

    fun registerComponent(clazz: Class<*>) {
        val componentMetadata = clazz.getAnnotation(Component::class.java) ?: return

        if (WorldComponent::class.java.isAssignableFrom(clazz)) {
            registeredWorldComponents.add(Pair(componentMetadata.tag, clazz))
        } else if (EntityComponent::class.java.isAssignableFrom(clazz)) {
            registeredEntityComponents.add(Pair(componentMetadata.tag, clazz))
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
