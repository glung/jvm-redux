package redux.api.adapters

import org.jetbrains.spek.api.Spek
import redux.api.Reducer
import redux.api.Store
import redux.api.helpers.Todos.State
import redux.api.makeCreateStoreTests


class BansaCreateStoreTest : Spek(makeCreateStoreTests(storeCreator()))

private fun storeCreator() = { reducer: Reducer<State>, initialState: State, enhancer: Store.Enhancer? ->
    BansaStoreAdapter.create(reducer, initialState, enhancer)
}
