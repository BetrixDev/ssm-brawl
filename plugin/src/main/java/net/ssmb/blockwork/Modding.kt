package net.ssmb.blockwork

class Modding {
    private val registeredAddedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()
    private val registeredRemovedListeners = arrayListOf<Pair<Class<*>, (obj: Any) -> Unit>>()

    @PublishedApi
    internal fun onListenerAdded(clazz: Class<*>, cb: (Any) -> Unit) {
        registeredAddedListeners.add(Pair(clazz, cb))
    }

    inline fun <reified T : Any> onListenerAdded(noinline cb: (obj: T) -> Unit) {
        @Suppress("UNCHECKED_CAST") onListenerAdded(T::class.java, cb as (Any) -> Unit)
    }

    @PublishedApi
    internal fun onListenerRemoved(clazz: Class<*>, cb: (Any) -> Unit) {
        registeredRemovedListeners.add(Pair(clazz, cb))
    }

    inline fun <reified T : Any> onListenerRemoved(noinline cb: (obj: T) -> Unit) {
        @Suppress("UNCHECKED_CAST") onListenerRemoved(T::class.java, cb as (Any) -> Unit)
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
