package redux.api.adapters

import redux.api.Reducer
import redux.api.Store

class ReduxKotlinStoreAdapter<S : Any>(val impl: redux.Store<S>) : Store<S> {

    override fun dispatch(action: Any): Any = impl.dispatch(action)

    override fun getState(): S = impl.getState()

    override fun subscribe(subscriber: Store.Subscriber): Store.Subscription {
        val subscription = impl.subscribe { subscriber.onStateChanged() }
        return Store.Subscription { subscription.unsubscribe()}
    }

    override fun replaceReducer(reducer: Reducer<S>) {
        impl.replaceReducer(toLibReducer(reducer))
    }

    companion object {
        fun <S : Any> create(reducer: Reducer<S>, initialState: S, enhancer: Store.Enhancer?): Store<S> {
            val impl: redux.Store<S> = redux.Store.create(toLibReducer(reducer), initialState, toLibEnhancer<S>(enhancer))
            return ReduxKotlinStoreAdapter(impl)
        }


        private fun <S : Any> toLibCreator(creator: Store.Creator): redux.Store.Creator<S> {
            return object : redux.Store.Creator<S> {
                override fun create(reducer: redux.Reducer<S>, initialState: S, enhancer: redux.Store.Enhancer<S>?): redux.Store<S> {
                    val store: Store<S> = creator.create(fromLibReducer(reducer), initialState, fromLibEnhancer(enhancer))
                    return object : redux.Store<S> {
                        override fun dispatch(action: Any): Any {
                            return store.dispatch(action)
                        }

                        override fun getState(): S {
                            return store.getState()
                        }

                        override fun replaceReducer(reducer: redux.Reducer<S>) {
                            store.replaceReducer(fromLibReducer(reducer))
                        }

                        override fun subscribe(subscriber: redux.Store.Subscriber): redux.Store.Subscription {
                            return toLibSubscribption(store.subscribe(fromLibSubscriber(subscriber)))
                        }

                    }
                }
            }
        }

        private fun toLibSubscribption(subscribe: Store.Subscription): redux.Store.Subscription {
            return object : redux.Store.Subscription {
                override fun unsubscribe() {
                    subscribe.unsubscribe()
                }
            }
        }

        private fun fromLibSubscriber(subscriber: redux.Store.Subscriber): Store.Subscriber {
            return object : Store.Subscriber {
                override fun onStateChanged() {
                    subscriber.onStateChanged()
                }
            }
        }

        private fun <S : Any> toLibEnhancer(enhancer: Store.Enhancer?): redux.Store.Enhancer<S>? {
            if (enhancer == null) {
                return null
            }

            return object : redux.Store.Enhancer<S> {
                override fun enhance(next: redux.Store.Creator<S>): redux.Store.Creator<S> {
                    val creator: Store.Creator = enhancer.enhance(fromLibCreator(next))
                    return toLibCreator(creator)
                }
            }
        }

        private fun <S : Any> fromLibEnhancer(enhancer: redux.Store.Enhancer<S>?): Store.Enhancer? {
            if (enhancer == null) {
                return null
            }

            return object : Store.Enhancer {
                override fun enhance(next: Store.Creator): Store.Creator {
                    val libCreator: redux.Store.Creator<S> = enhancer.enhance(toLibCreator(next))
                    return fromLibCreator(libCreator)
                }
            }
        }

        private fun <S : Any> fromLibReducer(reducer: redux.Reducer<S>): Reducer<S> {
            return object : Reducer<S> {
                override fun reduce(state: S, action: Any): S {
                    return reducer.reduce(state, action)
                }
            }
        }

        private fun <T : Any> fromLibCreator(next: redux.Store.Creator<T>): redux.api.Store.Creator {
            return object : redux.api.Store.Creator {
                override fun <S : Any> create(reducer: Reducer<S>, initialState: S, enhancer: Store.Enhancer?): Store<S> {
                    val libReducer: redux.Reducer<S> = toLibReducer(reducer)
                    val libEnhancer: redux.Store.Enhancer<S>? = toLibEnhancer(enhancer)
                    val libCreator = next as redux.Store.Creator<S>
                    val nextStore: redux.Store<S> = libCreator.create(libReducer, initialState, libEnhancer)

                    return if (nextStore is ReduxKotlinStoreAdapter<*>) {
                        nextStore as ReduxKotlinStoreAdapter<S>
                    } else {
                        ReduxKotlinStoreAdapter(nextStore)
                    }
                }
            }
        }

        private fun <S : Any> toLibReducer(reducer: Reducer<S>): redux.Reducer<S> {
            return object : redux.Reducer<S> {
                override fun reduce(state: S, action: Any): S = reducer.reduce(state, action)
            }
        }
    }
}