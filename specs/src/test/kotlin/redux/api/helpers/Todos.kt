package redux.api.helpers

object Todos {

    data class Todo(val id: Int, val name: String)

    data class State(val todos: List<Todo> = emptyList())

    sealed class Action {
        class AddTodo(val todo: String) : Action()
        class AddTodoAsync(val todo: String) : Action()
        class Unknown() : Action()
    }

}