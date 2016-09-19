package redux.api.adapters

import com.brianegan.bansa.BaseStore
import redux.api.Reducer
import redux.api.Store

class BansaStoreAdapter<S : Any>(val store: com.brianegan.bansa.Store<S>) : Store<S> {
    override fun getState(): S = store.state

    override fun dispatch(action: Any): Any {
        store.dispatch(ActionAdapter(action))
        return action
    }

    override fun subscribe(subscriber: Store.Subscriber): Store.Subscription {
        val subscription = store.subscribe { subscriber.onStateChanged() }
        return Store.Subscription { subscription.unsubscribe() }
    }

    override fun replaceReducer(reducer: Reducer<S>) = throw UnsupportedOperationException("not implemented")

    companion object {
        fun <S : Any> create(reducer: Reducer<S>, initialState: S, enhancer: Store.Enhancer?): Store<S> {
            return if (enhancer != null) {
                // Bansa does not support Enhancer yet.
                enhancer
                        .enhance(object : Store.Creator {
                            override fun <T : Any> create(reducer: Reducer<T>, initialState: T, enhancer: Store.Enhancer?): Store<T> {
                                return bansaStoreAdapter(initialState, reducer)
                            }
                        })
                        .create(reducer, initialState)
            } else {
                return bansaStoreAdapter(initialState, reducer)
            }
        }

        private fun <T : Any> bansaStoreAdapter(initialState: T, reducer: Reducer<T>): BansaStoreAdapter<T> {
            val libsStore = BaseStore<T>(initialState, toLibReducer(reducer))
            val store = BansaStoreAdapter(libsStore)
            // TODO : Remove when fixed in lib
            store.dispatch(Store.Companion.INIT)
            return store
        }

        private fun <S : Any> toLibReducer(reducer: Reducer<S>): com.brianegan.bansa.Reducer<S>? {
            return com.brianegan.bansa.Reducer<S> { state, action -> reducer.reduce(state, (action as ActionAdapter).action) }
        }
    }
}

class ActionAdapter(val action: Any) : com.brianegan.bansa.Action

