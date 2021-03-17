package GUI.AdditionalElements;

import Common.Conversation;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ContactLabel extends Label {
    private final Conversation conversation;
    private final VBox chatBox;
    private final Label titleLabel;
    private String lastAuthor;

    public ContactLabel(Conversation conversation) {
        super(conversation.getName());
        this.conversation = conversation;
        this.chatBox = new VBox();
        this.titleLabel = new Label(conversation.getName());
        this.titleLabel.setFont(new Font(20.0));
        this.titleLabel.setPadding(new Insets(10.0, 0.0, 0.0, 20.0));
        this.lastAuthor = "";

        AnchorPane.setLeftAnchor(chatBox, 0.0);
        AnchorPane.setRightAnchor(chatBox, 0.0);
        AnchorPane.setTopAnchor(chatBox, 0.0);
        AnchorPane.setBottomAnchor(chatBox, 60.0);
    }

    public ContactLabel(Conversation conversation, VBox chatBox) {
        super(conversation.getName());
        this.conversation = conversation;
        this.chatBox = chatBox;
        this.titleLabel = new Label(conversation.getName());
        this.titleLabel.setFont(new Font(20.0));
        this.titleLabel.setPadding(new Insets(10.0, 0.0, 0.0, 20.0));
        this.lastAuthor = "";

        AnchorPane.setLeftAnchor(chatBox, 0.0);
        AnchorPane.setRightAnchor(chatBox, 0.0);
        AnchorPane.setTopAnchor(chatBox, 0.0);
        AnchorPane.setBottomAnchor(chatBox, 60.0);
    }

    public Conversation getConversation() {
        return conversation;
    }

    public VBox getChatBox() {
        return chatBox;
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public void setLastAuthor(String lastAuthor) {
        this.lastAuthor = lastAuthor;
    }

    public String getLastAuthor() {
        return lastAuthor;
    }

}
