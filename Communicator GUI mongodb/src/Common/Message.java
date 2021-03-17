package Common;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private final String content;
    private final ObjectId id;
    private final ObjectId authorId;
    private final String authorName;
    private final ObjectId conversation;
    private final Date send_date;

    public Message(ObjectId id, String content, ObjectId authorId, String authorName, ObjectId conversation, Date send_date) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.conversation = conversation;
        this.send_date = send_date;
    }

    public ObjectId getId() {
        return id;
    }

    public String getContent() {
        return this.content;
    }

    public ObjectId getAuthorId() {
        return authorId;
    }

    public ObjectId getConversation() {
        return conversation;
    }

    public Date getSend_date() {
        return send_date;
    }

    public String getAuthorName() {
        return authorName;
    }

    @Override
    public String toString() {
        return "Message with id = " + id + ",\n" +
                "\tcontent = " + content +",\n" +
                "\tauthor = " + authorId + ",\n" +
                "\tconversation = " + conversation + ",\n" +
                "\tsend_date = " + send_date;
    }
}
