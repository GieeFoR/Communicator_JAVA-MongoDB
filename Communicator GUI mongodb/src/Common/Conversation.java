package Common;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Conversation implements Serializable {
    private static final long serialVersionUID = 1234567L;

    private final ObjectId id;
    private final ObjectId founder;
    private final List<ObjectId> clients;
    private final List<Message> messages;
    private final String name;
    private final String description;
    private final Date date;

    public Conversation(ObjectId id, String name, String description, ObjectId founder, List <ObjectId> clients, List <Message> messages, Date date)  {
        this.id = id;
        this.name = name;
        this.description = description;
        this.founder = founder;
        this.clients = clients;
        this.messages = messages;
        this.date = date;
    }

    public Conversation(ObjectId id, String name, String description, ObjectId founder, List <ObjectId> clients)  {
        this.id = id;
        this.name = name;
        this.description = description;
        this.founder = founder;
        this.clients = clients;
        this.messages = new ArrayList<>();
        this.date = new Date();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getName() {
        return name;
    }

    public ObjectId getId() {
        return id;
    }

    public List<ObjectId> getClients() {
        return clients;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ObjectId> getMessagesIds() {
        List <ObjectId> messagesIds = new LinkedList<>();

        for(Message m : messages) {
            messagesIds.add(m.getId());
        }
        return messagesIds;
    }

    public void addClient(ObjectId client) {
        this.clients.add(client);
    }

    public ObjectId getFounder() {
        return founder;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Conversation with id = " + id + ",\n" +
                "\tname = " + name + ",\n" +
                "\tfounder = " + founder + ",\n" +
                "\tclients = " + clients + ",\n" +
                "\tcreation date = " + date;
    }
}