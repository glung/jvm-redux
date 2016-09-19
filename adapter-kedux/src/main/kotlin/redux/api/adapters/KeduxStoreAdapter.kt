package redux.api.adapters

import redux.api.Reducer
import redux.api.Store

class KeduxStoreAdapter<S : Any>(val impl: com.angusmorton.kedux.Store<S, Any>) : Store<S> {
    override fun getState(): S = impl.state

    override fun dispatch(action: Any): Any {
        return impl.dispatch(action)
    }

    override fun subscribe(subscriber: Store.Subscriber): Store.Subscription {
        val subscription = impl.subscribe { subscriber.onStateChanged() }
        return Store.Subscription { subscription.unsubscribe() }
    }

    override fun replaceReducer(reducer: Reducer<S>): Unit = throw UnsupportedOperationException("not implemented")

    companion object {
        fun <S : Any> create(reducer: Reducer<S>, initialState: S, enhancer: Store.Enhancer?): Store<S> {
            return if (enhancer != null) {
                // edux does not support Enhancer yet.
                enhancer
                        .enhance(object : Store.Creator {
                            override fun <T : Any> create(reducer: Reducer<T>, initialState: T, enhancer: Store.Enhancer?): Store<T> =
                                    keduxStoreAdapter(initialState, reducer)
                        })
                        .create(reducer, initialState)
            } else {
                return keduxStoreAdapter(initialState, reducer)
            }
        }

        private fun <S : Any> keduxStoreAdapter(initialState: S, reducer: Reducer<S>): KeduxStoreAdapter<S> {
            val libState = initialState
            val libReducer = { s: S, a: Any -> reducer.reduce(s, a) }
            val libStore = com.angusmorton.kedux.Store.create(libState, libReducer)
            val store = KeduxStoreAdapter(libStore)
            // TODO : Remove when fixed in lib
            store.dispatch(Store.Companion.INIT)
            return store
        }
    }
}
