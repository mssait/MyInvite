package com.hionstudios.mypersonalinvite.Flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hionstudios.FirebaseNotificationService;
import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Event;
import com.hionstudios.mypersonalinvite.model.EventInvite;
import com.hionstudios.mypersonalinvite.model.EventMessage;
import com.hionstudios.mypersonalinvite.model.EventMessageRead;
import com.hionstudios.mypersonalinvite.model.FcmDeviceToken;
import com.hionstudios.mypersonalinvite.model.User;

@Service
public class ChatFlow {

    private final FirebaseNotificationService firebaseNotificationService;

    @Autowired
    public ChatFlow(FirebaseNotificationService firebaseNotificationService) {
        this.firebaseNotificationService = firebaseNotificationService;
    }

    public MapResponse sendMessage(long id, String messageText) {
        long senderId = 3;
        // Long senderId = UserUtil.getUserid();
        // if (senderId == null || senderId <= 0) {
        // return MapResponse.failure("User not authenticated");
        // }

        // Save the message to DB
        EventMessage message = new EventMessage();
        message.set("event_id", id);
        message.set("sender_id", senderId);
        message.set("message", messageText);
        message.insert();

        // Fetch all guests for the event
        List<EventInvite> invites = EventInvite.where("event_id = ?", id);
        if (invites.isEmpty()) {
            return MapResponse.success("Message sent - no guests found");
        }

        // Extract guest IDs (excluding sender)
        List<Long> guestIds = new ArrayList<>(
                invites.stream()
                        .map(inv -> inv.getLong("guest_id"))
                        .filter(guestId -> guestId != null && !guestId.equals(senderId))
                        .distinct()
                        .toList());

        Event event = Event.findById(id);
        if (event != null) {
            Long ownerId = event.getLong("owner_id");
            if (ownerId != null && !ownerId.equals(senderId) && !guestIds.contains(ownerId)) {
                guestIds.add(ownerId);
            }
        }

        // Fetch FCM tokens of all guests
        User sender = User.findById(senderId);
        String senderName = sender != null ? sender.getString("name") : "A participant";
        String notifTitle = "New message in event chat";
        String notifBody = senderName + ": " + messageText;

        // Fetch and send push notifications for all recipients
        for (Long receiverId : guestIds) {
            try {
                List<FcmDeviceToken> tokens = FcmDeviceToken.where(
                        "user_id = ? AND fcm_token IS NOT NULL AND fcm_token <> ''",
                        receiverId);

                for (FcmDeviceToken token : tokens) {
                    String fcmToken = token.getString("fcm_token");
                    if (fcmToken == null || fcmToken.isEmpty())
                        continue;

                    try {
                        firebaseNotificationService.sendNotification(
                                fcmToken,
                                notifTitle,
                                notifBody);
                    } catch (Exception e) {
                        System.err.println("FCM send failed for token " + fcmToken + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending notification to user " + receiverId + ": " + e.getMessage());
            }
        }

        return MapResponse.success("Message sent successfully").put("message_id", message.getLongId());
    }

    public MapResponse getMessages(long id, Long afterMessageId) {
        Long userId = UserUtil.getUserid();
        StringBuilder sql = new StringBuilder(
                "Select Event_Messages.*, Users.Id As sender_id, Users.Name As sender_name, Users.Profile_Pic From Event_Messages Join Users On Users.Id = Event_Messages.Sender_Id Where Event_Messages.Event_Id = ?");

        if (afterMessageId != null) {
            sql.append("And Event_Messages.id > ? ");
        }
        sql.append("Order By Event_Messages.created_time Asc");

        List<MapResponse> messages = afterMessageId == null
                ? Handler.findAll(sql.toString(), id)
                : Handler.findAll(sql.toString(), id, afterMessageId);

        for (MapResponse msg : messages) {
            msg.put("user_id", userId);
        }

        return MapResponse.success().put("messages", messages);
    }

    public MapResponse deleteMessage(Long id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        EventMessage message = EventMessage.findById(id);
        if (message == null) {
            return MapResponse.failure("Message not found");
        }

        if (!message.getLong("sender_id").equals(userId)) {
            return MapResponse.failure("You can only delete your own messages");
        }

        message.delete();
        return MapResponse.success("Message deleted");
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
        // Total Participants
        String participantSql = "Select Distinct Guest_Id As Id From Event_Invites Where Event_Id = ? Union Select Owner_Id As Id From Events Where Id = ?";
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

    public MapResponse getChatList() {
        Long userId = UserUtil.getUserid();

        String sql = "SELECT Events.id, Events.title, (SELECT Event_Thumbnails.image FROM Event_Thumbnails WHERE Event_Thumbnails.event_id = Events.id ORDER BY Event_Thumbnails.id ASC LIMIT 1) AS event_thumbnail, (SELECT jsonb_build_object('message', Event_Messages.message, 'last_msg_time', Event_Messages.created_time) FROM Event_Messages WHERE Event_Messages.event_id = Events.id ORDER BY Event_Messages.created_time DESC LIMIT 1) AS last_message FROM Events WHERE Events.owner_id = ? OR Events.id IN (SELECT Event_Invites.event_id FROM Event_Invites WHERE Event_Invites.guest_id = ?) ORDER BY Events.created_time DESC";

        List<MapResponse> chatList = Handler.findAll(sql, userId, userId);

        return MapResponse.success()
                .put("chats", chatList)
                .put("status", "success");
    }

}
