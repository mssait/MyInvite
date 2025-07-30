package com.hionstudios.mypersonalinvite.Flow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.EventMessage;
import com.hionstudios.mypersonalinvite.model.EventMessageRead;

public class ChatFlow {
    public MapResponse sendMessage(long event_id, String messageText) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        EventMessage message = new EventMessage();
        message.set("event_id", event_id);
        message.set("sender_id", userId);
        message.set("message", messageText);
        message.insert();

        return MapResponse.success("Message sent").put("message_id", message.getLongId());
    }

    public MapResponse getMessages(long event_id, Long afterMessageId) {
        StringBuilder sql = new StringBuilder(
                "Select Event_Messages.*, Users.Id As sender_id, Users.Name As sender_name, Users.Profile_Pic From Event_Messages Join Users On Users.Id = Event_Messages.Sender_Id Where Event_Messages.Event_Id = ?");

        if (afterMessageId != null) {
            sql.append("And Event_Messages.id > ? ");
        }
        sql.append("Order By Event_Messages.created_time Asc");

        List<MapResponse> messages = afterMessageId == null
                ? Handler.findAll(sql.toString(), event_id)
                : Handler.findAll(sql.toString(), event_id, afterMessageId);

        return MapResponse.success().put("messages", messages);
    }

    public MapResponse markAsRead(long message_id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("Not authenticated");

        // check if already marked
        EventMessageRead existing = EventMessageRead.findFirst(
                "message_id = ? And receipient_id = ?", message_id, userId);

        if (existing == null) {
            EventMessageRead read = new EventMessageRead();
            read.set("message_id", message_id);
            read.set("receipient_id", userId);
            read.insert();
        }

        return MapResponse.success("Marked as read");
    }

    public MapResponse getMessageReadStatus(long event_id, long message_id) {
        //Total Participants
        String participantSql = "Select Distinct Guest_Id As Id From Event_Guests Where Event_Id = ? Union Select Owner_Id As Id FROM Events Where Id = ?";
        List<MapResponse> participants = Handler.findAll(participantSql, event_id, event_id);

        Set<Long> allUsers = participants.stream()
                .map(p -> p.getLong("id"))
                .collect(Collectors.toSet());

        // 2. Readers
        String readSql = "Select Distinct Receipient_Id From Event_Message_Reads Where Message_Id = ?";
        List<MapResponse> reads = Handler.findAll(readSql, message_id);

        Set<Long> readUsers = reads.stream()
                .map(r -> r.getLong("receipient_id"))
                .collect(Collectors.toSet());

        // 3. Compute unread users
        Set<Long> notSeenUsers = new HashSet<>(allUsers);
        notSeenUsers.removeAll(readUsers);

        boolean allRead = notSeenUsers.isEmpty();

        return MapResponse.success()
                .put("message_id", message_id)
                .put("total_participants", allUsers.size())
                .put("read_count", readUsers.size())
                .put("unread_count", notSeenUsers.size())
                .put("all_users_read", allRead)
                .put("unread_user_ids", notSeenUsers);
    }

}
