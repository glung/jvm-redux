// We have to cheat on the package visibility because of some interoperability issue
// The internal CoreStore is required to be accessible.
// TODO : Remove when fixed in lib
package com.redux

import redux.api.Reducer
import redux.api.Store

class ReduxJavaStoreAdapter<S : Any>(val impl: com.redux.Store<LibAction, LibState<S>>) : Store<S> {

    override fun getState(): S = impl.state.state

    override fun subscribe(subscriber: Store.Subscriber): Store.Subscription {
        val subscription = impl.subscribe { subscriber.onStateChanged() }
        return Store.Subscription { subscription.unsubscribe() }
    }

    override fun replaceReducer(reducer: Reducer<S>): Unit = throw UnsupportedOperationException("not implemented")

    override fun dispatch(action: Any): Any {
        impl.dispatch(LibAction(action))
        return action
    }

    companion object {
        fun <S : Any> create(reducer: Reducer<S>, initialState: S, enhancer: Store.Enhancer?): Store<S> {
            return if (enhancer != null) {
                //  Redux-Java does not support Enhancer.
                enhancer
                        .enhance(object : Store.Creator {
                            override fun <T : Any> create(reducer: Reducer<T>, initialState: T, enhancer: Store.Enhancer?): Store<T> {
                                return reduxJavaStoreAdapter(initialState, reducer)
                            }
                        })
                        .create(reducer, initialState)
            } else {
                return reduxJavaStoreAdapter(initialState, reducer)
            }

        }

        private fun <S : Any> reduxJavaStoreAdapter(initialState: S, reducer: Reducer<S>): ReduxJavaStoreAdapter<S> {
            val libState: LibState<S> = LibState(initialState)
            val libReducer: com.redux.Reducer<LibAction, LibState<S>> = toLibReducer(reducer)
            val libStore: com.redux.Store<LibAction, LibState<S>> = com.redux.Store.create(libState, libReducer)
            val store = ReduxJavaStoreAdapter(libStore)
            // TODO : Remove when fixed in lib
            store.dispatch(Store.Companion.INIT)
            return store
        }

        private fun <S : Any> toLibReducer(reducer: Reducer<S>) =
                com.redux.Reducer { a: LibAction, s: LibState<S> ->
                    LibState(reducer.reduce(s.state, a.action))
                }

    }
}

class LibAction(val action: Any) : com.redux.Action
class LibState<out S : Any>(val state: S) : com.redux.State
