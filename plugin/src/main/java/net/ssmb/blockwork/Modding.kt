package net.ssmb.blockwork

class Modding {
    @PublishedApi
    internal val registeredAddedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()
    @PublishedApi
    internal val registeredRemovedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()

    inline fun <reified T : Any> onListenerAdded(noinline cb: (obj: T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        registeredAddedListeners.add(Pair(T::class.java, cb as (Any) -> Unit))
    }

    inline fun <reified T : Any> onListenerRemoved(noinline cb: (obj: T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        registeredRemovedListeners.add(Pair(T::class.java, cb as (Any) -> Unit))
    }

    fun handleListenerAdded(obj: Any) {
        registeredAddedListeners.forEach { (inter, cb) ->
            if (inter.isAssignableFrom(obj::class.java)) {
                cb(obj)
            }
        }
    }

    fun handleListenerRemoved(obj: Any) {
        registeredRemovedListeners.forEach { (inter, cb) ->
            if (inter.isAssignableFrom(obj::class.java)) {
                cb(obj)
            }
        }
    }
}
