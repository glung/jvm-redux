package redux.api.adapters

import com.beyondeye.reduks.StoreSubscriber
import redux.api.Reducer
import redux.api.Store

class ReduksStoreAdapter<S : Any>(val impl: com.beyondeye.reduks.Store<S>) : Store<S> {
    override fun getState(): S = impl.state

    override fun dispatch(action: Any): Any = impl.dispatch(action)

    override fun subscribe(subscriber: Store.Subscriber): Store.Subscription {
        val subscription = impl.subscribe(StoreSubscriber<S> { subscriber.onStateChanged() })
        return Store.Subscription { subscription.unsubscribe() }
    }

    override fun replaceReducer(reducer: Reducer<S>) {
        impl.replaceReducer(toLibReducer(reducer))
    }

    companion object {
        fun <S : Any> create(reducer: redux.api.Reducer<S>, initialState: S, enhancer: Store.Enhancer?): Store<S> {
            return if (enhancer != null) {
                // Reduks does not support Enhancer yet.
                enhancer
                        .enhance(object : Store.Creator {
                            override fun <T : Any> create(reducer: Reducer<T>, initialState: T, enhancer: Store.Enhancer?): Store<T> =
                                    reduksStoreAdapter(initialState, reducer)
                        })
                        .create(reducer, initialState)
            } else {
                return reduksStoreAdapter(initialState, reducer)
            }
        }

        private fun <S : Any> reduksStoreAdapter(initialState: S, reducer: Reducer<S>): ReduksStoreAdapter<S> {
            val libStore = com.beyondeye.reduks.SimpleStore(initialState, toLibReducer(reducer))
            val store = ReduksStoreAdapter(libStore)
            // The lib does not do it
            store.dispatch(Store.Companion.INIT)
            return store
        }

        private fun <S : Any> toLibReducer(reducer: Reducer<S>): com.beyondeye.reduks.Reducer<S> {
            return com.beyondeye.reduks.Reducer<S> { s: S, action: Any ->
                reducer.reduce(s, action)
            }
        }
    }
}