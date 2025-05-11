package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

import java.util.*;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "chat_room")
public class ChatRoomEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean isPrivate;
    private boolean isChatBot;
    private boolean isFinanceBot;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = AppConstants.TABLE_PREFIX + "rooms_users")
    private final Set<User> users = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "room")
    private final List<MessageEntity> messages = new ArrayList<>();

    public void addUser(User u) {
        users.add(u);
    }

    public void removeUser(User u) {
        users.remove(u);
    }

    public void addMessage(MessageEntity message) {
        messages.add(message);
        message.setRoom(this);
        this.lastMessage = message;
    }

    @Transient
    private MessageEntity lastMessage;
}
