package Server;

import Common.Client;
import Common.Conversation;
import Common.Message;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ObjectsAdaptor {
    public static Document toDocument(Client client) {
        return new Document("_id", client.getId())
                .append("number", client.getNumber())
                .append("name", client.getName())
                .append("surname", client.getSurname())
                .append("username", client.getUsername())
                .append("password", client.getPassword())
                .append("creationDate", client.getCreationDate())
                .append("conversations", client.getConversationsIds())
                .append("email", client.geteMail());
    }

    public static Document toDocument(Conversation conversation) {
        return new Document("_id", conversation.getId())
                .append("name", conversation.getName())
                .append("description", conversation.getDescription())
                .append("founder", conversation.getFounder())
                .append("clients", conversation.getClients())
                .append("messages", conversation.getMessagesIds())
                .append("creationDate", conversation.getDate());
    }

    public static Document toDocument(Message message) {
        return new Document("_id", message.getId())
                .append("content", message.getContent())
                .append("author", message.getAuthorId())
                .append("conversation", message.getConversation())
                .append("sendDate", message.getSend_date());
    }

    public static Client toClient(Document document) {
        return new Client(
                document.getObjectId("_id"),
                document.getInteger("number"),
                document.getString("username"),
                document.getString("password"),
                document.getString("name"),
                document.getString("surname"),
                document.getDate("creationDate"),
                document.getString("email")
        );
    }

    public static Conversation toConversation(Document document) {
        return toConversation(document, new LinkedList<>());
    }

    public static Conversation toConversation(Document document, List<Message> messages) {
        return new Conversation(
                document.getObjectId("_id"),
                document.getString("name"),
                document.getString("description"),
                document.getObjectId("founder"),
                (List<ObjectId>) document.get("clients"),
                messages,
                document.getDate("creationDate")
        );
    }

    public static Message toMessage(Document document) {
        return new Message(
                document.getObjectId("_id"),
                document.getString("content"),
                document.getObjectId("author"),
                getClientUsername(document.getObjectId("author")),
                document.getObjectId("conversation"),
                document.getDate("sendDate")
        );
    }

    public static List<Client> getClients() {
        List<Client> clients = new LinkedList<>();

        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find().iterator();

        while(iterator.hasNext()) {
            clients.add(ObjectsAdaptor.toClient(iterator.next()));
        }
        return clients;
    }

    public static List<Conversation> getConversations() {
        List<Conversation> conversations = new LinkedList<>();

        Iterator<Document> iterator =
                Server.getInstance().getConversationCollection().find().iterator();

        while(iterator.hasNext()) {
            conversations.add(ObjectsAdaptor.toConversation(iterator.next()));
        }
        return conversations;
    }

    public static List<Message> getMessages() {
        List<Message> messages = new LinkedList<>();

        Iterator<Document> iterator =
                Server.getInstance().getMessageCollection().find().iterator();

        while(iterator.hasNext()) {
            messages.add(ObjectsAdaptor.toMessage(iterator.next()));
        }
        return messages;
    }

    public static Client getClient(Integer number) {
        return getClientField("number", number);
    }

    public static Client getClient(ObjectId id) {
        return getClientField("_id", id);
    }

    public static Client getClient(String username) {
        return getClientField("username", username);
    }

    private static <T> Client getClientField(String fieldName, T data) {
        Client client = null;

        Document documentToFind = new Document(fieldName, data);
        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find(documentToFind).limit(1).iterator();

        while(iterator.hasNext()) {
            client = ObjectsAdaptor.toClient(iterator.next());
        }
        return client;
    }

    public static Client getClient(String username, String password) {
        Client client = null;
        Document documentToFind = new Document("username", username).append("password", password);
        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find(documentToFind).iterator();

        while(iterator.hasNext()) {
            client = ObjectsAdaptor.toClient(iterator.next());
        }
        return client;
    }

    public static boolean checkUsernameIsTaken(String username) {
        boolean result = false;
        Document documentToFind = new Document("username", username);
        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find(documentToFind).iterator();

        if(iterator.hasNext()) {
            result = true;
        }
        return result;
    }

    public static boolean checkEmailIsTaken(String email) {
        boolean result = false;
        Document documentToFind = new Document("email", email);
        Iterator <Document> iterator = Server.getInstance().getUserCollection().find(documentToFind).iterator();

        if(iterator.hasNext()) {
            result = true;
        }
        return result;
    }

    public static Client getClient_ByName(String name, String surname) {
        Client client = null;
        Document documentToFind = new Document("name", name).append("surname", surname);
        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find(documentToFind).iterator();

        while(iterator.hasNext()) {
            client = ObjectsAdaptor.toClient(iterator.next());
        }
        return client;
    }

    public static List<Conversation> getClientConversations(Integer number) {
        return getClientConversations("number", number);
    }

    public static List<Conversation> getClientConversations(ObjectId id) {
        return getClientConversations("_id", id);
    }

    private static <T> List<Conversation> getClientConversations(String fieldName, T data) {
        List<Conversation> conversations = new LinkedList<>();

        String joinFieldName = "user";
        Document lookup = lookupDocument("User", "clients", "_id", joinFieldName);
        Document match = matchDocument(joinFieldName+"."+fieldName, data);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookup);
        pipeline.add(match);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getConversationCollection()
                        .aggregate(pipeline).iterator();

        while(iterator.hasNext()) {
            Document document = iterator.next();
            List <Message> messages = getConversationMessages(document.getObjectId("_id"));
            conversations.add(ObjectsAdaptor.toConversation(document, messages));
        }
        return conversations;
    }

    public static List<Conversation> getConversationsFoundedByUser_withNumber(Integer number) {
        List<Conversation> conversations = new LinkedList<>();

        //join User to Conversation
        String joinFieldName = "founder_joined";
        Document lookup = lookupDocument("User", "founder", "_id", joinFieldName);
        Document match = matchDocument(joinFieldName+".number", number);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookup);
        pipeline.add(match);

        //find conversations founded by client with number
        Iterator<Document> iterator =
                Server.getInstance()
                        .getConversationCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            Document document = iterator.next();
            List <Message> messages = getConversationMessages(document.getObjectId("_id"));
            conversations.add(ObjectsAdaptor.toConversation(document, messages));
        }
        return conversations;
    }

    public static List<Conversation> getConversationsFoundedByUser_withId(ObjectId id) {
        List<Conversation> conversations = new LinkedList<>();

        //find conversations founded by client with id
        Document documentToFind = new Document("founder", id);
        Iterator<Document> iterator =
                Server.getInstance()
                        .getConversationCollection()
                        .find(documentToFind)
                        .iterator();

        while(iterator.hasNext()) {
            Document document = iterator.next();
            List <Message> messages = getConversationMessages(document.getObjectId("_id"));
            conversations.add(ObjectsAdaptor.toConversation(document, messages));
        }
        return conversations;
    }

    public static List<Message> getConversationMessages(ObjectId id) {
        List <Message> messages = new LinkedList<>();

        Document documentToFind = new Document("conversation", id);
        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .find(documentToFind)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static List<Message> getClientMessages_withRegex(Integer number, String regex) {
        List <Message> messages = new LinkedList<>();

        String authorField = "authorJoined";
        Document lookup = lookupDocument("User", "author", "_id", authorField);

        Document matchClient = matchDocument(authorField+".number", number);

        String found = "found";
        Document addFields = new Document("$addFields",
                new Document(found,
                        new Document("$regexMatch",
                                new Document("input", "$content")
                                .append("regex", ".*" + regex + ".*"))));

        Document matchFound = matchDocument(found, true);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookup);
        pipeline.add(matchClient);
        pipeline.add(addFields);
        pipeline.add(matchFound);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static List<Message> getClientMessages_withRegex(ObjectId id, String regex) {
        List <Message> messages = new LinkedList<>();

        String authorField = "authorJoined";
        Document lookup = lookupDocument("User", "author", "_id", authorField);

        Document matchClient = matchDocument(authorField+"._id", id);

        String found = "found";
        Document addFields = new Document("$addFields",
                new Document(found,
                        new Document("$regexMatch",
                                new Document("input", "$content")
                                        .append("regex", ".*" + regex + ".*"))));

        Document matchFound = matchDocument(found, true);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookup);
        pipeline.add(matchClient);
        pipeline.add(addFields);
        pipeline.add(matchFound);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static List<Message> getClientMessagesInConversation_withRegex(Integer clientNumber, ObjectId conversationId, String regex) {
        List <Message> messages = new LinkedList<>();

        String authorField = "authorJoined";
        Document lookupClient = lookupDocument("User", "author", "_id", authorField);

        String conversationField = "conversation_joined";
        Document lookupConversation = lookupDocument("Conversation", "conversation", "_id", conversationField);

        Document matchClientAndConversation = matchDocument(authorField+".number", clientNumber).append(conversationField+"._id", conversationId);

        String found = "found";
        Document addFields = new Document("$addFields",
                new Document(found,
                        new Document("$regexMatch",
                                new Document("input", "$content")
                                        .append("regex", ".*" + regex + ".*"))));

        Document matchFound = matchDocument(found, true);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookupClient);
        pipeline.add(lookupConversation);
        pipeline.add(matchClientAndConversation);
        pipeline.add(addFields);
        pipeline.add(matchFound);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static List<Message> getClientMessagesInConversation_withRegex(ObjectId clientId, ObjectId conversationId, String regex) {
        List <Message> messages = new LinkedList<>();

        String authorField = "authorJoined";
        Document lookupClient = lookupDocument("User", "author", "_id", authorField);

        String conversationField = "conversation_joined";
        Document lookupConversation = lookupDocument("Conversation", "conversation", "_id", conversationField);

        Document matchClientAndConversation = matchDocument(authorField+"._id", clientId).append(conversationField+"._id", conversationId);

        String found = "found";
        Document addFields = new Document("$addFields",
                new Document(found,
                        new Document("$regexMatch",
                                new Document("input", "$content")
                                        .append("regex", ".*" + regex + ".*"))));

        Document matchFound = matchDocument(found, true);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookupClient);
        pipeline.add(lookupConversation);
        pipeline.add(matchClientAndConversation);
        pipeline.add(addFields);
        pipeline.add(matchFound);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static List<Message> getMessagesInConversation_withRegex(ObjectId conversationId, String regex) {
        List <Message> messages = new LinkedList<>();

        String conversationField = "conversation_joined";
        Document lookupConversation = lookupDocument("Conversation", "conversation", "_id", conversationField);

        Document matchClientAndConversation = matchDocument(conversationField+"._id", conversationId);

        String found = "found";
        Document addFields = new Document("$addFields",
                new Document(found,
                        new Document("$regexMatch",
                                new Document("input", "$content")
                                        .append("regex", ".*" + regex + ".*"))));

        Document matchFound = matchDocument(found, true);

        List <Document> pipeline = new LinkedList<>();
        pipeline.add(lookupConversation);
        pipeline.add(matchClientAndConversation);
        pipeline.add(addFields);
        pipeline.add(matchFound);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static List<Client> getClientsConversation(ObjectId id) {
        List<Client> clients = new LinkedList<>();

        String clients_joined = "clients_joined";
        Document match = matchDocument("_id", id);
        Document lookup = lookupDocument("User", "clients", "_id", clients_joined);

        List<Document> pipeline = new LinkedList<>();
        pipeline.add(match);
        pipeline.add(lookup);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getConversationCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            List<Document> docs = (List<Document>) iterator.next().get(clients_joined);
            for(Document doc : docs) {
                clients.add(toClient(doc));
            }
        }
        return clients;
    }

    public static Client getLastClient() {
        Client client = null;

        Document sort = new Document("creationDate", -1);
        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find().sort(sort).limit(1).iterator();

        while(iterator.hasNext()) {
            client = ObjectsAdaptor.toClient(iterator.next());
        }
        return client;
    }

    public static List<Client> getClientByCreationDate(Date date) {
        List<Client> clients = new LinkedList<>();
        String dateToFind = new SimpleDateFormat("yyyy-MM-dd").format(date);

        String dayFieldName = "creationDay";
        Document addFields = new Document("$addFields",
                new Document(dayFieldName,
                        new Document("$dateToString",
                                new Document("format", "%Y-%m-%d")
                                .append("date", "$creationDate"))));
        Document match = matchDocument(dayFieldName, new Document("$eq", dateToFind));

        List<Document> pipeline = new LinkedList<>();
        pipeline.add(addFields);
        pipeline.add(match);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getUserCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            clients.add(toClient(iterator.next()));
        }
        return clients;
    }

    public static List<Message> getClientMessagesSendOn(Integer number, Date date) {
        List <Message> messages = new LinkedList<>();
        String dateToFind = new SimpleDateFormat("yyyy-MM-dd").format(date);

        String authorJoined = "authorJoined";
        Document lookup = lookupDocument("User", "author", "_id", authorJoined);

        Document matchClient = matchDocument(authorJoined+"."+"number", number);

        String dayFieldName = "sendDay";
        Document addFields = new Document("$addFields",
                new Document(dayFieldName,
                        new Document("$dateToString",
                                new Document("format", "%Y-%m-%d")
                                        .append("date", "$sendDate"))));

        Document dayMatch = matchDocument(dayFieldName, new Document("$eq", dateToFind));

        List<Document> pipeline = new LinkedList<>();
        pipeline.add(lookup);
        pipeline.add(matchClient);
        pipeline.add(addFields);
        pipeline.add(dayMatch);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            messages.add(toMessage(iterator.next()));
        }
        return messages;
    }

    public static Message getClientLastMessage(Integer number) {
        Message message = null;

        String authorJoined = "authorJoined";
        Document lookup = lookupDocument("User", "author", "_id", authorJoined);

        Document matchClient = matchDocument(authorJoined+"."+"number", number);

        Document sort = new Document("$sort", new Document("sendDate", -1));
        Document limit = new Document("$limit", 1);

        List<Document> pipeline = new LinkedList<>();
        pipeline.add(lookup);
        pipeline.add(matchClient);
        pipeline.add(sort);
        pipeline.add(limit);

        Iterator<Document> iterator =
                Server.getInstance()
                        .getMessageCollection()
                        .aggregate(pipeline)
                        .iterator();

        while(iterator.hasNext()) {
            message = toMessage(iterator.next());
        }

        return message;
    }

    public static void insertMessage(Message message) {
        Document document = toDocument(message);
        Server.getInstance().getMessageCollection().insertOne(document);

        Server.getInstance().getConversationCollection()
                .updateOne(
                        new Document("_id", message.getConversation()),
                        Updates.addToSet("messages", message.getId()));
    }

    public static void insertConversation(Conversation conversation, List<ObjectId> clients) {
        Document document = toDocument(conversation);

        Server.getInstance().getConversationCollection().insertOne(document);

        for(ObjectId cl : clients) {
            Server.getInstance().getUserCollection()
                    .updateOne(
                            new Document("_id", cl),
                            Updates.addToSet("conversations", conversation.getId()));
        }
    }

    public static void insertClient(Client client) {
        Document document = toDocument(client);
        Server.getInstance().getUserCollection().insertOne(document);
    }

    private static String getClientUsername(ObjectId id) {
        Document documentToFind = new Document("_id", id);
        Iterator<Document> iterator =
                Server.getInstance().getUserCollection().find(documentToFind).iterator();

        String username = null;
        while(iterator.hasNext()) {
            username = iterator.next().getString("username");
        }
        return username;
    }

    private static Document lookupDocument(String from, String localField, String foreignField, String as) {
        return new Document("$lookup",
                new Document("from", from)
                .append("localField", localField)
                .append("foreignField", foreignField)
                .append("as", as));
    }

    private static <T> Document matchDocument(String fieldName, T data) {
        return new Document("$match",
                new Document(fieldName, data));
    }
}
