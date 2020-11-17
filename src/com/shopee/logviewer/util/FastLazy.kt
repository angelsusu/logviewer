package com.shopee.logviewer.util

import java.util.concurrent.atomic.AtomicReference
import java.io.Serializable

/**
 * @Author junzhang
 * @Time 2020/5/29
 *
 * Maybe initial twice, but ensure that you will get the same instance all the time.
 */

fun <T> fastLazy(initializer: () -> T): Lazy<T> = FastLazyImpl(initializer)

private class FastInitializedLazyImpl<out T>(override val value: T) : Lazy<T>, Serializable {

    override fun isInitialized(): Boolean = true

    override fun toString(): String = value.toString()

}

private class FastLazyImpl<out T>(private val initializer: () -> T) : Lazy<T>, Serializable {

    enum class LazyState {
        UN_INITIALIZED, INITIALIZING, COMPLETE
    }

    //private var initializer: (() -> T)? = initializer
    @Volatile private var _value: Any? = LazyState.UN_INITIALIZED
    private val state = AtomicReference<LazyState>(LazyState.UN_INITIALIZED)

    override val value: T
        get() {
            if (state.get() == LazyState.COMPLETE) {
                @Suppress("UNCHECKED_CAST")
                return _value as T
            }

            if (state.compareAndSet(LazyState.UN_INITIALIZED, LazyState.INITIALIZING)) {
                _value = initializer.invoke()
                state.set(LazyState.COMPLETE)
            }

            while (state.get() != LazyState.COMPLETE) {} // spin

            @Suppress("UNCHECKED_CAST")
            return _value as T
        }


    override fun isInitialized(): Boolean = _value !== LazyState.UN_INITIALIZED

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    fun writeReplace(): Any = FastInitializedLazyImpl(value)
}


