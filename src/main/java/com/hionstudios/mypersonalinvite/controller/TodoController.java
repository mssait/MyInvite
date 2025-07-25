package com.hionstudios.mypersonalinvite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.TodoFlow;

@RestController
@RequestMapping("api/todo")
public class TodoController {
    @PostMapping("add/{event_id}")
    @IsUser
    public ResponseEntity<MapResponse> addTodo(
            @PathVariable int event_id,
            @RequestParam String task) {
        return ((DbTransaction) () -> new TodoFlow().addTodo(event_id, task)).write();
    }

    @PutMapping("update/{id}")
    @IsUser
    public ResponseEntity<MapResponse> updateTodo(
            @PathVariable int id,
            @RequestParam(required = false) String task,
            @RequestParam(required = false) Boolean status) {
        return ((DbTransaction) () -> new TodoFlow().updateTodo(id, task, status)).write();
    }

    @DeleteMapping("delete/{id}")
    @IsUser
    public ResponseEntity<MapResponse> deleteTodo(@PathVariable int id) {
        return ((DbTransaction) () -> new TodoFlow().deleteTodo(id)).write();
    }

    @GetMapping("list/{event_id}")
    @IsUser
    public ResponseEntity<MapResponse> listTodos(@PathVariable int event_id) {
        return ((DbTransaction) () -> new TodoFlow().listTodos(event_id)).read();
    }
}
