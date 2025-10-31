package com.hionstudios.mypersonalinvite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.ChatFlow;

@RestController
@RequestMapping("api/chat")
public class ChatController {
    @PostMapping("event/{id}/send")
    // @IsUser
    public ResponseEntity<MapResponse> sendMessage(
            @PathVariable long id,
            @RequestParam String messageText) {
        return ((DbTransaction) () -> new ChatFlow().sendMessage(id, messageText)).write();
    }

    @GetMapping("event/{id}")
    @IsUser
    public ResponseEntity<MapResponse> getMessages(
            @PathVariable long id,
            @RequestParam(required = false) Long after_message_id) {
        return ((DbTransaction) () -> new ChatFlow().getMessages(id, after_message_id)).read();
    }

    @GetMapping("list")
    @IsUser
    public ResponseEntity<MapResponse> getChatList() {
        return ((DbTransaction) () -> new ChatFlow().getChatList()).read();
    }

    @DeleteMapping("{id}/delete")
    @IsUser
    public ResponseEntity<MapResponse> deleteMessage(
            @PathVariable long id) {
        return ((DbTransaction) () -> new ChatFlow().deleteMessage(id)).write();
    }
    

    @PostMapping("mark-read")
    @IsUser
    public ResponseEntity<MapResponse> markAsRead(
            @RequestParam long message_id) {
        return ((DbTransaction) () -> new ChatFlow().markAsRead(message_id)).write();
    }

    @GetMapping("message-read-status")
    @IsUser
    public ResponseEntity<MapResponse> getMessageReadStatus(
            @RequestParam long event_id,
            @RequestParam long message_id) {
        return ((DbTransaction) () -> new ChatFlow().getMessageReadStatus(event_id, message_id)).read();
    }

}
