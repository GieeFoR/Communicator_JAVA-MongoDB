package Common;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Client extends Account implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private final ObjectId id;
    private final Integer number;
    private final String name;
    private final String surname;
    List<Conversation> conversations;

    public Client(ObjectId id, Integer number, String login, String password, String name, String surname, Date creationDate, String eMail) {
        super(login, password, eMail, creationDate);
        this.number = number;
        this.id = id;
        this.name = name;
        this.surname = surname;
        conversations = new ArrayList<>();
    }

    public ObjectId getId() {
        return id;
    }

    public void addConversation(Conversation conversation) {
        conversations.add(conversation);
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public List<ObjectId> getConversationsIds() {
        List <ObjectId> conversationIds = new LinkedList<>();

        for(Conversation c : conversations) {
            conversationIds.add(c.getId());
        }
        return conversationIds;
    }

    public Integer getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}