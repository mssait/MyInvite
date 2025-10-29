package com.hionstudios.mypersonalinvite.Flow;

import java.util.List;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.mypersonalinvite.model.EventTodoList;

public class TodoFlow {

    public MapResponse addTodo(Long id, String task) {

        EventTodoList todo = new EventTodoList();
        todo.set("event_id", id);
        todo.set("todo", task);
        todo.insert();

        return MapResponse.success("To-do added");
    }

    public MapResponse updateTodo(Long id, String task, Boolean status) {
        EventTodoList todo = EventTodoList.findById(id);
        if (todo == null)
            return MapResponse.failure("To-do not found");

        if (task != null) {
            todo.set("todo", task);
        }

        if (status != null) {
            todo.set("status", status);
        }

        todo.saveIt();
        return MapResponse.success("To-do updated");
    }

    public MapResponse deleteTodo(Long id) {
        EventTodoList todo = EventTodoList.findById(id);
        if (todo == null)
            return MapResponse.failure("To-do not found");
        todo.delete();
        return MapResponse.success("To-do deleted");
    }

    public MapResponse listTodos(Long id) {

        String sql = "Select * From Event_Todo_Lists Where Event_Id = ?";

        List<MapResponse> todos = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("todos", todos);
        return response;
    }
}
