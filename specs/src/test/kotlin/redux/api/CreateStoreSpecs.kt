package redux.api

import org.jetbrains.spek.api.DescribeBody
import org.mockito.Mockito.*
import redux.api.Store.Subscriber
import redux.api.helpers.ActionCreators.addTodo
import redux.api.helpers.ActionCreators.unknownAction
import redux.api.helpers.Reducers
import redux.api.helpers.Todos.State
import redux.api.helpers.Todos.Todo
import kotlin.test.expect

// TODO : finish porting tests from https://github.com/reactjs/redux/blob/master/test
fun makeCreateStoreTests(createStore: (reducer: Reducer<State>, initialState: State, enhancer: Store.Enhancer?) -> Store<State>): DescribeBody.() -> Unit {

    fun createStore(todos: Reducer<State>, state: State): Store<State> = createStore(todos, state, null)

    return {
        describe("createStore") {
            
            it("passes the initial action and the initial state") {
                val store = createStore(Reducers.TODOS, State(
                        listOf(
                                Todo(1, "Hello")
                        )
                ))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello")
                            )
                    )
                }
            }

            it("applies the reducer to the previous state") {
                val store = createStore(Reducers.TODOS, State())
                expect(store.getState()) { State() }

                store.dispatch(unknownAction())
                expect(store.getState()) { State() }

                store.dispatch(addTodo("Hello"))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello")
                            )
                    )
                }

                store.dispatch(addTodo("World"))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello"),
                                    Todo(2, "World")
                            )
                    )
                }
            }

            it("applies the reducer to the initial state") {
                val store = createStore(Reducers.TODOS, State(
                        listOf(
                                Todo(1, "Hello")
                        )
                ))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello")
                            )
                    )
                }

                store.dispatch(unknownAction())
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello")
                            )
                    )
                }

                store.dispatch(addTodo("World"))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello"),
                                    Todo(2, "World")
                            )
                    )
                }

            }

            it ("sends init when replacing a reducer") {
                val store = createStore(Reducers.TODOS, State())
                var receivedAction : Any? = null
                store.replaceReducer(Reducer { state: State, action: Any ->
                    receivedAction = action
                    state
                })
                expect(Store.Companion.INIT) {receivedAction}
            }

            it("preserves the state when replacing a reducer") {
                val store = createStore(Reducers.TODOS, State())
                store.dispatch(addTodo("Hello"))
                store.dispatch(addTodo("World"))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello"),
                                    Todo(2, "World")
                            )
                    )
                }

                store.replaceReducer(Reducers.TODOS_REVERSE)
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(1, "Hello"),
                                    Todo(2, "World")
                            )
                    )
                }

                store.dispatch(addTodo("Perhaps"))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(3, "Perhaps"),
                                    Todo(1, "Hello"),
                                    Todo(2, "World")
                            )
                    )
                }

                store.replaceReducer(Reducers.TODOS)
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(3, "Perhaps"),
                                    Todo(1, "Hello"),
                                    Todo(2, "World")
                            )
                    )
                }

                store.dispatch(addTodo("Surely"))
                expect(store.getState()) {
                    State(
                            listOf(
                                    Todo(3, "Perhaps"),
                                    Todo(1, "Hello"),
                                    Todo(2, "World"),
                                    Todo(4, "Surely")
                            )
                    )
                }

            }

            it("supports multiple subscriptions") {
                val store = createStore(Reducers.TODOS, State())
                val subscriberA = mock(Subscriber::class.java)
                val subscriberB = mock(Subscriber::class.java)

                val subscriptionA = store.subscribe(subscriberA)
                store.dispatch(unknownAction())
                verify(subscriberA, times(1)).onStateChanged()
                verify(subscriberB, times(0)).onStateChanged()

                store.dispatch(unknownAction())
                verify(subscriberA, times(2)).onStateChanged()
                verify(subscriberB, times(0)).onStateChanged()

                val subscriptionB = store.subscribe(subscriberB)
                store.dispatch(unknownAction())
                verify(subscriberA, times(3)).onStateChanged()
                verify(subscriberB, times(1)).onStateChanged()

                subscriptionA.unsubscribe()
                verify(subscriberA, times(3)).onStateChanged()
                verify(subscriberB, times(1)).onStateChanged()

                store.dispatch(unknownAction())
                verify(subscriberA, times(3)).onStateChanged()
                verify(subscriberB, times(2)).onStateChanged()

                subscriptionB.unsubscribe()
                verify(subscriberA, times(3)).onStateChanged()
                verify(subscriberB, times(2)).onStateChanged()

                store.dispatch(unknownAction())
                verify(subscriberA, times(3)).onStateChanged()
                verify(subscriberB, times(2)).onStateChanged()

                val subscriptionA2 = store.subscribe(subscriberA)
                verify(subscriberA, times(3)).onStateChanged()
                verify(subscriberB, times(2)).onStateChanged()

                store.dispatch(unknownAction())
                verify(subscriberA, times(4)).onStateChanged()
                verify(subscriberB, times(2)).onStateChanged()
            }

            it("supports higher order function subscriptions") {
                val store = createStore(Reducers.TODOS, State())
                var onStateChangedCalled = false

                store.dispatch(unknownAction())
                assert(!onStateChangedCalled)

                val subscription = store.subscribe {
                    onStateChangedCalled = true
                }

                store.dispatch(unknownAction())
                assert(onStateChangedCalled)
            }

            it("only removes listener once when unsubscribe is called") {
                val store = createStore(Reducers.TODOS, State())
                val subscriberA = mock(Subscriber::class.java)
                val subscriberB = mock(Subscriber::class.java)

                val subscriptionA = store.subscribe(subscriberA)
                store.subscribe(subscriberB)

                subscriptionA.unsubscribe()
                subscriptionA.unsubscribe()

                store.dispatch(unknownAction())
                verify(subscriberA, times(0)).onStateChanged()
                verify(subscriberB, times(1)).onStateChanged()
            }

        }
    }

//            it("only removes relevant listener when unsubscribe is called") {
//                val store = createStore(reducers.todos)
//                val listener = expect.createSpy(() => {})
//
//                store.subscribe(listener)
//                val unsubscribeSecond = store.subscribe(listener)
//
//                unsubscribeSecond()
//                unsubscribeSecond()
//
//                store.dispatch(unknownAction())
//                expect(listener.calls.length).toBe(1)
//            })
//
//            it("supports removing a subscription within a subscription") {
//                val store = createStore(reducers.todos)
//                val listenerA = expect.createSpy(() => {})
//                val listenerB = expect.createSpy(() => {})
//                val listenerC = expect.createSpy(() => {})
//
//                store.subscribe(listenerA)
//                val unSubB = store.subscribe(() => {
//                listenerB()
//                unSubB()
//            })
//                store.subscribe(listenerC)
//
//                store.dispatch(unknownAction())
//                store.dispatch(unknownAction())
//
//                expect(listenerA.calls.length).toBe(2)
//                expect(listenerB.calls.length).toBe(1)
//                expect(listenerC.calls.length).toBe(2)
//            })
//
//            it("delays unsubscribe until the end of current dispatch") {
//                val store = createStore(reducers.todos)
//
//                val unsubscribeHandles = []
//                val doUnsubscribeAll = () => unsubscribeHandles.forEach(
//                unsubscribe => unsubscribe()
//                )
//
//                val listener1 = expect.createSpy(() => {})
//                val listener2 = expect.createSpy(() => {})
//                val listener3 = expect.createSpy(() => {})
//
//                unsubscribeHandles.push(store.subscribe(() => listener1()))
//                unsubscribeHandles.push(store.subscribe(() => {
//                    listener2()
//                    doUnsubscribeAll()
//                }))
//                unsubscribeHandles.push(store.subscribe(() => listener3()))
//
//                store.dispatch(unknownAction())
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(1)
//                expect(listener3.calls.length).toBe(1)
//
//                store.dispatch(unknownAction())
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(1)
//                expect(listener3.calls.length).toBe(1)
//            })
//
//            it("delays subscribe until the end of current dispatch") {
//                val store = createStore(reducers.todos)
//
//                val listener1 = expect.createSpy(() => {})
//                val listener2 = expect.createSpy(() => {})
//                val listener3 = expect.createSpy(() => {})
//
//                let listener3Added = false
//                val maybeAddThirdListener = () => {
//                if (!listener3Added) {
//                    listener3Added = true
//                    store.subscribe(() => listener3())
//                }
//            }
//
//                store.subscribe(() => listener1())
//                store.subscribe(() => {
//                    listener2()
//                    maybeAddThirdListener()
//                })
//
//                store.dispatch(unknownAction())
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(1)
//                expect(listener3.calls.length).toBe(0)
//
//                store.dispatch(unknownAction())
//                expect(listener1.calls.length).toBe(2)
//                expect(listener2.calls.length).toBe(2)
//                expect(listener3.calls.length).toBe(1)
//            })
//
//            it("uses the last snapshot of subscribers during nested dispatch") {
//                val store = createStore(reducers.todos)
//
//                val listener1 = expect.createSpy(() => {})
//                val listener2 = expect.createSpy(() => {})
//                val listener3 = expect.createSpy(() => {})
//                val listener4 = expect.createSpy(() => {})
//
//                let unsubscribe4
//                        val unsubscribe1 = store.subscribe(() => {
//                listener1()
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(0)
//                expect(listener3.calls.length).toBe(0)
//                expect(listener4.calls.length).toBe(0)
//
//                unsubscribe1()
//                unsubscribe4 = store.subscribe(listener4)
//                store.dispatch(unknownAction())
//
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(1)
//                expect(listener3.calls.length).toBe(1)
//                expect(listener4.calls.length).toBe(1)
//            })
//                store.subscribe(listener2)
//                store.subscribe(listener3)
//
//                store.dispatch(unknownAction())
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(2)
//                expect(listener3.calls.length).toBe(2)
//                expect(listener4.calls.length).toBe(1)
//
//                unsubscribe4()
//                store.dispatch(unknownAction())
//                expect(listener1.calls.length).toBe(1)
//                expect(listener2.calls.length).toBe(3)
//                expect(listener3.calls.length).toBe(3)
//                expect(listener4.calls.length).toBe(1)
//            })
//
//            it("provides an up-to-date state when a subscriber is notified", done => {
//                val store = createStore(reducers.todos)
//                store.subscribe(() => {
//                    expect(store.getState()).toEqual([
//                    {
//                        id: 1,
//                        text: "Hello"
//                    }
//                    ])
//                    done()
//                })
//                store.dispatch(addTodo("Hello"))
//            })
//
//            it("only accepts plain object actions") {
//                val store = createStore(reducers.todos)
//                expect(() =>
//                store.dispatch(unknownAction())
//                ).toNotThrow()
//
//                function AwesomeMap() { }
//                [ null, undefined, 42, "hey", new AwesomeMap() ].forEach(nonObject =>
//                expect(() =>
//                store.dispatch(nonObject)
//                ).toThrow(/plain/)
//                )
//            })
//
//            it("handles nested dispatches gracefully") {
//                function foo(state = 0, action) {
//                return action.type === "foo" ? 1 : state
//            }
//
//                function bar(state = 0, action) {
//                return action.type === "bar" ? 2 : state
//            }
//
//                val store = createStore(combineReducers({ foo, bar }))
//
//                store.subscribe(function kindaComponentDidUpdate() {
//                    val state = store.getState()
//                    if (state.bar === 0) {
//                        store.dispatch({ type: "bar" })
//                    }
//                })
//
//                store.dispatch({ type: "foo" })
//                expect(store.getState()).toEqual({
//                    foo: 1,
//                    bar: 2
//                })
//            })
//
//            it("does not allow dispatch() from within a reducer") {
//                val store = createStore(reducers.dispatchInTheMiddleOfReducer)
//
//                expect(() =>
//                store.dispatch(dispatchInMiddle(store.dispatch.bind(store, unknownAction())))
//                ).toThrow(/may not dispatch/)
//            })
//
//            it("recovers from an error within a reducer") {
//                val store = createStore(reducers.errorThrowingReducer)
//                expect(() =>
//                store.dispatch(throwError())
//                ).toThrow()
//
//                expect(() =>
//                store.dispatch(unknownAction())
//                ).toNotThrow()
//            })
//
//            it("throws if action type is missing") {
//                val store = createStore(reducers.todos)
//                expect(() =>
//                store.dispatch({})
//                ).toThrow(/Actions may not have an undefined "type" property/)
//            })
//
//            it("throws if action type is undefined") {
//                val store = createStore(reducers.todos)
//                expect(() =>
//                store.dispatch({ type: undefined })
//                ).toThrow(/Actions may not have an undefined "type" property/)
//            })
//
//            it("does not throw if action type is falsy") {
//                val store = createStore(reducers.todos)
//                expect(() =>
//                store.dispatch({ type: false })
//                ).toNotThrow()
//                expect(() =>
//                store.dispatch({ type: 0 })
//                ).toNotThrow()
//                expect(() =>
//                store.dispatch({ type: null })
//                ).toNotThrow()
//                expect(() =>
//                store.dispatch({ type: "" })
//                ).toNotThrow()
//            })
//
//            it("accepts enhancer as the third argument") {
//                val emptyArray = []
//                val spyEnhancer = vanillaCreateStore => (...args) => {
//                expect(args[0]).toBe(reducers.todos)
//                expect(args[1]).toBe(emptyArray)
//                expect(args.length).toBe(2)
//                val vanillaStore = vanillaCreateStore(...args)
//                return {
//                    ...vanillaStore,
//                    dispatch: expect.createSpy(vanillaStore.dispatch).andCallThrough()
//                }
//            }
//
//                val store = createStore(reducers.todos, emptyArray, spyEnhancer)
//                val action = addTodo("Hello")
//                store.dispatch(action)
//                expect(store.dispatch).toHaveBeenCalledWith(action)
//                expect(store.getState()).toEqual([
//                {
//                    id: 1,
//                    text: "Hello"
//                }
//                ])
//            })
//
//            it("accepts enhancer as the second argument if initial state is missing") {
//                val spyEnhancer = vanillaCreateStore => (...args) => {
//                expect(args[0]).toBe(reducers.todos)
//                expect(args[1]).toBe(undefined)
//                expect(args.length).toBe(2)
//                val vanillaStore = vanillaCreateStore(...args)
//                return {
//                    ...vanillaStore,
//                    dispatch: expect.createSpy(vanillaStore.dispatch).andCallThrough()
//                }
//            }
//
//                val store = createStore(reducers.todos, spyEnhancer)
//                val action = addTodo("Hello")
//                store.dispatch(action)
//                expect(store.dispatch).toHaveBeenCalledWith(action)
//                expect(store.getState()).toEqual([
//                {
//                    id: 1,
//                    text: "Hello"
//                }
//                ])
//            })
//
//            it("throws if enhancer is neither undefined nor a function") {
//                expect(() =>
//                createStore(reducers.todos, undefined, {})
//                ).toThrow()
//
//                expect(() =>
//                createStore(reducers.todos, undefined, [])
//                ).toThrow()
//
//                expect(() =>
//                createStore(reducers.todos, undefined, null)
//                ).toThrow()
//
//                expect(() =>
//                createStore(reducers.todos, undefined, false)
//                ).toThrow()
//
//                expect(() =>
//                createStore(reducers.todos, undefined, undefined)
//                ).toNotThrow()
//
//                expect(() =>
//                createStore(reducers.todos, undefined, x => x)
//                ).toNotThrow()
//
//                expect(() =>
//                createStore(reducers.todos, x => x)
//                ).toNotThrow()
//
//                expect(() =>
//                createStore(reducers.todos, [])
//                ).toNotThrow()
//
//                expect(() =>
//                createStore(reducers.todos, {})
//                ).toNotThrow()
//            })
//
//            it("throws if nextReducer is not a function") {
//                val store = createStore(reducers.todos)
//
//                expect(() =>
//                store.replaceReducer()
//                ).toThrow("Expected the nextReducer to be a function.")
//
//                expect(() =>
//                store.replaceReducer(() => {})
//                ).toNotThrow()
//            })
//
//            it("throws if listener is not a function") {
//                val store = createStore(reducers.todos)
//
//                expect(() =>
//                store.subscribe()
//                ).toThrow()
//
//                expect(() =>
//                store.subscribe("")
//                ).toThrow()
//
//                expect(() =>
//                store.subscribe(null)
//                ).toThrow()
//
//                expect(() =>
//                store.subscribe(undefined)
//                ).toThrow()
//            })
//
//            describe("Symbol.observable interop point") {
//                it("should exist") {
//                    val store = createStore(() => {})
//                    expect(typeof store[$$observable]).toBe("function")
//                })
//
//                describe("returned value") {
//                    it("should be subscribable") {
//                        val store = createStore(() => {})
//                        val obs = store[$$observable]()
//                        expect(typeof obs.subscribe).toBe("function")
//                    })
//
//                    it("should throw a TypeError if an observer object is not supplied to subscribe") {
//                        val store = createStore(() => {})
//                        val obs = store[$$observable]()
//
//                        expect(function () {
//                            obs.subscribe()
//                        }).toThrow()
//
//                        expect(function () {
//                            obs.subscribe(() => {})
//                        }).toThrow()
//
//                        expect(function () {
//                            obs.subscribe({})
//                        }).toNotThrow()
//                    })
//
//                    it("should return a subscription object when subscribed") {
//                        val store = createStore(() => {})
//                        val obs = store[$$observable]()
//                        val sub = obs.subscribe({})
//                        expect(typeof sub.unsubscribe).toBe("function")
//                    })
//                })
//
//                it("should pass an integration test with no unsubscribe") {
//                    function foo(state = 0, action) {
//                    return action.type === "foo" ? 1 : state
//                }
//
//                    function bar(state = 0, action) {
//                    return action.type === "bar" ? 2 : state
//                }
//
//                    val store = createStore(combineReducers({ foo, bar }))
//                    val observable = store[$$observable]()
//                    val results = []
//
//                    observable.subscribe({
//                        next(state) {
//                            results.push(state)
//                        }
//                    })
//
//                    store.dispatch({ type: "foo" })
//                    store.dispatch({ type: "bar" })
//
//                    expect(results).toEqual([ { foo: 0, bar: 0 }, { foo: 1, bar: 0 }, { foo: 1, bar: 2 } ])
//                })
//
//                it("should pass an integration test with an unsubscribe") {
//                    function foo(state = 0, action) {
//                    return action.type === "foo" ? 1 : state
//                }
//
//                    function bar(state = 0, action) {
//                    return action.type === "bar" ? 2 : state
//                }
//
//                    val store = createStore(combineReducers({ foo, bar }))
//                    val observable = store[$$observable]()
//                    val results = []
//
//                    val sub = observable.subscribe({
//                        next(state) {
//                            results.push(state)
//                        }
//                    })
//
//                    store.dispatch({ type: "foo" })
//                    sub.unsubscribe()
//                    store.dispatch({ type: "bar" })
//
//                    expect(results).toEqual([ { foo: 0, bar: 0 }, { foo: 1, bar: 0 } ])
//                })
//
//                it("should pass an integration test with a common library (RxJS)") {
//                    function foo(state = 0, action) {
//                    return action.type === "foo" ? 1 : state
//                }
//
//                    function bar(state = 0, action) {
//                    return action.type === "bar" ? 2 : state
//                }
//
//                    val store = createStore(combineReducers({ foo, bar }))
//                    val observable = Rx.Observable.from(store)
//                    val results = []
//
//                    val sub = observable
//                            .map(state => ({ fromRx: true, ...state }))
//                    .subscribe(state => results.push(state))
//
//                    store.dispatch({ type: "foo" })
//                    sub.unsubscribe()
//                    store.dispatch({ type: "bar" })
//
//                    expect(results).toEqual([ { foo: 0, bar: 0, fromRx: true }, { foo: 1, bar: 0, fromRx: true } ])
//                })
//            })
//        }
//    }
}


