package GUI.Controllers;

import GUI.AdditionalElements.ContactLabel;
import GUI.DEV.Start;
import GUI.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import Client.ClientConnection;
import Client.ClientStatic;
import Common.*;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

public class CommunicatorController {
    public HBox firstCol;
    public HBox secondCol;
    ExecutorService executorService;
    ScheduledExecutorService scheduledExecutorService;
    ScheduledFuture<String> timer = null;
    private boolean slowMode = false;
    private List<ContactLabel> contactLabels;
    private ContactLabel activeConversation = null;

    private Semaphore mutex;

    private EventHandler<MouseEvent> clickOnContactEvent;

    private Insets leftInset;
    private Insets rightInset;

    private List<String> usernamesInNewContact = null;

    Comparator<? super Node> contactLabelComparator =
            (Node n1, Node n2) -> {
            ContactLabel cl1 = (ContactLabel) n1;
            ContactLabel cl2 = (ContactLabel) n2;
        return cl1.getConversation().getName().toLowerCase().compareTo(cl2.getConversation().getName().toLowerCase());
    };

    //slow mode off task
    TimerTask counterTask = new TimerTask() {
        @Override
        public void run() {
        }
    };

    //slow mode on task
    TimerTask antyspamTask = new TimerTask() {
        @Override
        public void run() {
        }
    };

    private int messagesCounter = 0;

    @FXML
    private AnchorPane mainPane;
    @FXML
    private GridPane clientName;
    @FXML
    private TextField searchField;
    @FXML
    private VBox contactsListBox;
    @FXML
    private Pane titlePane;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private TextArea typeMessageTextArea;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private  AnchorPane addContactPane;
    @FXML
    private TextField numberTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private Button addUserButton;
    @FXML
    private TextField contactNameTextField;
    @FXML
    private VBox newContactUsersList;

    /**
     * Initialize window
     */
    public void initialize() {
        executorService = ClientConnection.getInstance().getExecutorService();
        scheduledExecutorService = ClientConnection.getInstance().getScheduledExecutorService();
        mutex = new Semaphore(1);

        contactLabels = new ArrayList<>();

        leftInset = new Insets(0.0, 100.0, 0.0, 5.0);
        rightInset = new Insets(0.0, 20.0, 0.0, 100.0);

        usernamesInNewContact = new LinkedList<>();

        executorService.submit(() -> {
            boolean initialized = false;
            while(true) {
                if(ClientStatic.getInstance().getClient() != null) {
                    if(!initialized) {
                        initialized = true;
                        createContacts();
                        createUsernameLabel();
                    }
                    listenForMessages();
                }
                Thread.sleep(10);
            }
        });

        clickOnContactEvent = event -> {
            addContactPane.setVisible(false);
            chatPane.setVisible(true);
            activeConversation = (ContactLabel) event.getSource();
            chatScrollPane.setContent(activeConversation.getChatBox());
            chatScrollPane.setVvalue(1.0);
            titlePane.getChildren().clear();
            titlePane.getChildren().add(activeConversation.getTitleLabel());

            //set black color (all messages read)
            activeConversation.setTextFill(Color.BLACK);
        };

        filterTextField();
    }

    private void createUsernameLabel() {
        Label usernameLabel = new Label(ClientStatic.getInstance().getClient().getUsername());
        usernameLabel.setPadding(new Insets(0.0, 0.0, 0.0, 10.0));
        usernameLabel.setFont(new Font(30.0));

        Platform.runLater(() -> clientName.getChildren().add(usernameLabel));
    }

    private void createContacts() {
        Platform.runLater(() -> {
            for(Conversation c : ClientStatic.getInstance().getClient().getConversations()) {
                VBox chatBox = new VBox();

                ContactLabel contact = new ContactLabel(c, chatBox);
                contact.setPrefSize(260.0, 40.0);
                contact.setMinSize(260.0, 40.0);
                contact.setLayoutX(20);
                contact.setAlignment(Pos.CENTER);
                contact.setTextAlignment(TextAlignment.CENTER);
                contact.setOnMouseClicked(clickOnContactEvent);

                contactLabels.add(contact);
                contactsListBox.getChildren().add(contact);
                chatScrollPane.setContent(contact.getChatBox());

                TextFlow flow;
                HBox hbox;
                for(Message m : c.getMessages()) {
                    Text text = new Text(m.getContent());
                    text.setFill(Color.BLACK);
                    text.setFont(new Font(13));

                    flow = new TextFlow();
                    flow.getChildren().add(text);
                    flow.setTextAlignment(TextAlignment.LEFT);

                    hbox = new HBox(flow);
                    hbox.prefWidthProperty().bind(chatScrollPane.widthProperty());

                    if(m.getAuthorId().equals(ClientStatic.getInstance().getClient().getId())) {
                        //set text alignment to the right side of window
                        hbox.setAlignment(Pos.CENTER_RIGHT);
                        hbox.setPadding(rightInset);
                    }
                    else {
                        if(contact.getLastAuthor() == null || !contact.getLastAuthor().equals(m.getAuthorName())) {
                            //show message author username
                            Text messageAuthor = new Text("<" + m.getAuthorName() + ">");
                            messageAuthor.setTextAlignment(TextAlignment.LEFT);
                            messageAuthor.setFont(new Font(11));
                            messageAuthor.setFill(Color.GRAY);
                            chatBox.getChildren().add(messageAuthor);
                        }



                        //set text alignment to the left side of window
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.setPadding(leftInset);
                    }
                    contact.setLastAuthor(m.getAuthorName());
                    chatBox.getChildren().add(hbox);
                    flow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }
            }
            ObservableList<Node> workingCollection = FXCollections.observableArrayList(contactsListBox.getChildren());
            workingCollection.sort(contactLabelComparator);
            contactsListBox.getChildren().setAll(workingCollection);
        });
    }

    private void filterTextField() {
        // do not show tab in textField
        typeMessageTextArea.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(keyEvent.getCode() == KeyCode.TAB){
                keyEvent.consume();
            }
        });

        // enter = send; shift+enter = new line in textField
        typeMessageTextArea.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent1 -> {
            if(keyEvent1.getCode() == KeyCode.ENTER){
                if(keyEvent1.isShiftDown()) {
                    if(typeMessageTextArea.getLength() == 0) {
                        keyEvent1.consume();
                        return;
                    }
                    if(typeMessageTextArea.getText().charAt(typeMessageTextArea.getLength()-1) == '\n') {
                        keyEvent1.consume();
                        return;
                    }

                    typeMessageTextArea.setText(typeMessageTextArea.getText() + "\n");
                    typeMessageTextArea.positionCaret(typeMessageTextArea.getLength());
                }
                else {
                    if (!typeMessageTextArea.getText().trim().equals("")) {
                        checkAntyspam();
                    }
                    typeMessageTextArea.requestFocus();
                    keyEvent1.consume();
                }
            }
        });

        numberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numberTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        numberTextField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) {
                change.setText("");
            }
            return change;
        }));

        usernameTextField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) {
                change.setText("");
            }
            return change;
        }));

        contactNameTextField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) {
                change.setText("");
            }
            return change;
        }));
    }

    //send message on push 'send' button
    public void sendMessageAction() {
        if(typeMessageTextArea.getText().trim().equals("")) {
            typeMessageTextArea.requestFocus();
            return;
        }
        checkAntyspam();
    }

    //function check if message is a spam
    private void checkAntyspam() {
        //limit of fast messages before launch slow mode
        if(messagesCounter == 5) {
            //if slow mode is off switch it on
            if(!slowMode) {
                slowMode = true;
                chatPane.setPadding(new Insets(20.0,0.0,0.0,0.0));

                Label slowModeAlert_new = new Label("SLOW MODE: ON");
                slowModeAlert_new.setAlignment(Pos.CENTER);
                slowModeAlert_new.setPrefHeight(20.0);

                Tooltip toolTipSlowModeAlert = new Tooltip("You can send messages in every 2 seconds\n Restart application for turn off slow mode");
                slowModeAlert_new.setTooltip(toolTipSlowModeAlert);

                mainPane.getChildren().add(slowModeAlert_new);
                AnchorPane.setLeftAnchor(slowModeAlert_new, 300.0);
                AnchorPane.setRightAnchor(slowModeAlert_new, 20.0);
                AnchorPane.setTopAnchor(slowModeAlert_new, 50.0);
            }
            //if you want send message too fast in slow mode
            if(!timer.isDone()) {
                return;
            }
            //if you can send message start timer
            else {
                timer = (ScheduledFuture<String>) scheduledExecutorService.schedule(antyspamTask, 2, TimeUnit.SECONDS);
            }
        }
        //if slow mode is off
        else {
            if (timer != null) {
                //if you send message too fast increase the counter
                if (!timer.isDone()) {
                    messagesCounter++;
                }
                //if message is not fast reset counter
                else {
                    messagesCounter = 0;
                }
            }
            //start timer
            timer = (ScheduledFuture<String>) scheduledExecutorService.schedule(counterTask, 500, TimeUnit.MILLISECONDS);
        }

        try {
            sendMessage();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage() throws IOException, ClassNotFoundException {
        String content = typeMessageTextArea.getText().trim();
        Message message = new Message(
                new ObjectId(),
                content,
                ClientStatic.getInstance().getClient().getId(),
                ClientStatic.getInstance().getClient().getUsername(),
                activeConversation.getConversation().getId(),
                new Date());

        ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.MESSAGE);
        ClientConnection.getInstance().getObjectOutputSendStream().writeObject(message);

        ResponseType response;
        response = (ResponseType) ClientConnection.getInstance().getObjectInputSendStream().readObject();

        if(response.checkType(ResponseType.CONFIRMATION)) {
            showMessage(message.getAuthorName(), activeConversation, message.getContent());
        }
        else if(response.checkType(ResponseType.FAILURE)) {
            String title = "Add Contact Warning";
            String header = "Cannot send message";
            String alertContent = "";
            SceneManager.getInstance().warning(title, header, alertContent);
            //reconnect
        }
        else {
            System.out.println("ERROR #100");
            System.exit(-1);
        }
    }

    public void listenForMessages() throws IOException, ClassNotFoundException {
        InformationType informationType;
        Message message;

        informationType = (InformationType) ClientConnection.getInstance().getObjectInputReceiveStream().readObject();

        if(informationType.checkType(InformationType.MESSAGE)) {
            message = (Message) ClientConnection.getInstance().getObjectInputReceiveStream().readObject();
            for(ContactLabel c : contactLabels) {
                if(c.getConversation().getId().equals(message.getConversation())) {
                    showMessage(message.getAuthorName(), c, message.getContent());
                    break;
                }
            }
        }
        else if(informationType.checkType(InformationType.NEW_CONVERSATION)) {
            try {
                Conversation conversation = (Conversation) ClientConnection.getInstance().getObjectInputReceiveStream().readObject();
                ClientStatic.getInstance().getClient().addConversation(conversation);

                ClientConnection.getInstance().getObjectOutputReceiveStream().writeObject(ResponseType.CONFIRMATION);

                ContactLabel contact = new ContactLabel(conversation);
                contact.setPrefSize(260.0, 40.0);
                contact.setMinSize(260.0, 40.0);
                contact.setLayoutX(20);
                contact.setAlignment(Pos.CENTER);
                contact.setTextAlignment(TextAlignment.CENTER);
                contact.setOnMouseClicked(clickOnContactEvent);

                contactLabels.add(contact);

                Platform.runLater(() -> contactsListBox.getChildren().add(contact));
            } catch (ClassNotFoundException e) {
                ClientConnection.getInstance().getObjectOutputReceiveStream().writeObject(ResponseType.FAILURE);
            }
        }
    }

    private void showMessage(String authorUsername, ContactLabel contactLabel, String message) {
        Text text = new Text(message);
        text.setFill(Color.BLACK);
        text.setFont(new Font(13));

        //set text alignment in textflow
        TextFlow flow = new TextFlow();
        flow.getChildren().add(text);
        flow.setTextAlignment(TextAlignment.LEFT);

        HBox hbox = new HBox(flow);
        hbox.prefWidthProperty().bind(chatScrollPane.widthProperty());

        if(authorUsername.equals(ClientStatic.getInstance().getClient().getUsername())) {
            typeMessageTextArea.setText("");
            typeMessageTextArea.requestFocus();

            //set text alignment to the right side of window
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.setPadding(rightInset);

            try {
                mutex.acquire();
                contactLabel.getChatBox().getChildren().add(hbox);
                flow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                Platform.runLater(() -> chatScrollPane.setVvalue(1.0));

            }
            catch (InterruptedException e) {
                e.getMessage();
            }
            finally {
                mutex.release();
            }
        }
        else {
            if(!contactLabel.getLastAuthor().equals(authorUsername)) {
                //show message author username
                Text messageAuthor = new Text("<" + authorUsername + ">");
                messageAuthor.setTextAlignment(TextAlignment.LEFT);
                messageAuthor.setFont(new Font(11));
                messageAuthor.setFill(Color.GRAY);
                Platform.runLater(() -> contactLabel.getChatBox().getChildren().add(messageAuthor));
            }

            //set text alignment to the left side of window
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(leftInset);

            if(chatScrollPane.getVvalue() == 1.0) {
                executorService.submit(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
                });
            }

            Platform.runLater(() -> {
                try {
                    mutex.acquire();
                    contactLabel.getChatBox().getChildren().add(hbox);
                    flow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                } finally {
                    mutex.release();
                }
            });


            if(!activeConversation.equals(contactLabel)) {
                contactLabel.setTextFill(Color.RED);
            }
        }
        contactLabel.setLastAuthor(authorUsername);
    }

    public void onSearchContactsAction() {
        executorService.submit(() -> {
            for(ContactLabel cl : contactLabels) {
                if(searchField.getText().length() <= cl.getConversation().getName().length()) {
                    if(cl.getConversation().getName().toLowerCase().startsWith(searchField.getText().toLowerCase())) {
                        if(!contactsListBox.getChildren().contains(cl)) {
                            Platform.runLater(() -> {
                                contactsListBox.getChildren().add(cl);
                                ObservableList<Node> workingCollection = FXCollections.observableArrayList(contactsListBox.getChildren());
                                workingCollection.sort(contactLabelComparator);
                                contactsListBox.getChildren().setAll(workingCollection);
                            });
                        }
                    }
                    else {
                        if(contactsListBox.getChildren().contains(cl)) {
                            Platform.runLater(() -> {
                                contactsListBox.getChildren().remove(cl);
                                ObservableList<Node> workingCollection = FXCollections.observableArrayList(contactsListBox.getChildren());
                                workingCollection.sort(contactLabelComparator);
                                contactsListBox.getChildren().setAll(workingCollection);
                            });
                        }
                    }
                }
                else {
                    if(contactsListBox.getChildren().contains(cl)) {
                        Platform.runLater(() -> contactsListBox.getChildren().remove(cl));
                    }
                }
            }
        });
    }

    public void addContact() throws IOException, ClassNotFoundException {
        if(contactNameTextField.getText().trim().equals("")) {
            String title = "Add Contact Warning";
            String header = "Cannot add a new contact";
            String content = "Contact name field is empty. Type a contact name";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(contactNameTextField.getText().length() > 32) {
            String title = "Add Contact Warning";
            String header = "Cannot add a new contact";
            String content = "Contact name is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.NEW_CONVERSATION);
        ClientConnection.getInstance().getObjectOutputSendStream().writeObject(usernamesInNewContact);
        ClientConnection.getInstance().getObjectOutputSendStream().writeObject(contactNameTextField.getText());

        ResponseType response = (ResponseType) ClientConnection.getInstance().getObjectInputSendStream().readObject();

        if(response.checkType(ResponseType.CONFIRMATION)) {
            Conversation conversation = (Conversation) ClientConnection.getInstance().getObjectInputSendStream().readObject();

            ClientStatic.getInstance().getClient().addConversation(conversation);
            createContactLabel(conversation);
        }
        else if(response.checkType(ResponseType.FAILURE)) {
            String title = "Add Contact Warning";
            String header = "Cannot add a new contact";
            String content = "Adding a contact failed";
            SceneManager.getInstance().warning(title, header, content);
        }
        usernamesInNewContact.clear();
        Platform.runLater(() -> newContactUsersList.getChildren().clear());
    }

    //add contact to contacts list on push 'add' button
    public void showContactPaneAction() {
        addContactPane.setVisible(true);
        chatPane.setVisible(false);
        Platform.runLater(() -> titlePane.getChildren().clear());
    }

    public void numberButtonAction() {
        usernameTextField.setVisible(false);
        numberTextField.setVisible(true);
        addUserButton.setVisible(true);
    }

    public void nicknameButtonAction() {
        usernameTextField.setVisible(true);
        numberTextField.setVisible(false);
        addUserButton.setVisible(true);
    }

    public void addUserAction() throws IOException, ClassNotFoundException {
        if(usernameTextField.isVisible()) {
            if(usernameTextField.getText().trim().equals("")) {
                String title = "Add Contact Warning";
                String header = "Cannot add a new contact";
                String content = "Username field is empty. Type a username";
                SceneManager.getInstance().warning(title, header, content);
                return;
            }

            if(usernameTextField.getText().length() > 32) {
                String title = "Add Contact Warning";
                String header = "Cannot add a new contact";
                String content = "Username is too long";
                SceneManager.getInstance().warning(title, header, content);
                return;
            }

            ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.FIND_USER_BY_NICKNAME);
            ClientConnection.getInstance().getObjectOutputSendStream().writeObject(usernameTextField.getText().trim());

            ResponseType response = (ResponseType) ClientConnection.getInstance().getObjectInputSendStream().readObject();

            if(response.checkType(ResponseType.CONFIRMATION)) {
                String username = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                String number = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                String name = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                String surname = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();

                if(usernamesInNewContact.contains(username)) {
                    String title = "Add Contact Warning";
                    String header = "Cannot add a new contact member";
                    String content = "Member already added to new contact";
                    SceneManager.getInstance().warning(title, header, content);
                    return;
                }

                if(username.equals(ClientStatic.getInstance().getClient().getUsername())) {
                    String title = "Add Contact Warning";
                    String header = "Cannot add a new contact member";
                    String content = "You cannot add yourself to contact. If you want make conversation available only for you, do not add anyone";
                    SceneManager.getInstance().warning(title, header, content);
                    return;
                }

                HBox hBox = new HBox();
                hBox.getChildren().add(new Text(username + "\t" + number + "\t\t\t" + name + " " + surname));

                Button button = new Button("-");

                button.setOnAction(e -> {
                    if(newContactUsersList.getChildren().contains(hBox)) {
                        Platform.runLater(() -> newContactUsersList.getChildren().remove(hBox));
                    }

                    usernamesInNewContact.remove(username);
                });

                hBox.getChildren().add(button);
                Platform.runLater(() -> newContactUsersList.getChildren().add(hBox));
                usernamesInNewContact.add(username);
            }
            else if(response.checkType(ResponseType.FAILURE)) {
                String title = "Add Contact Warning";
                String header = "Cannot add a new contact member";
                String content = "We cannot find the user. Check correctness of new contact username";
                SceneManager.getInstance().warning(title, header, content);
            }
        }
        else if(numberTextField.isVisible()) {
            if(numberTextField.getText().equals("")) {
                String title = "Add Contact Warning";
                String header = "Cannot add a new contact";
                String content = "Number field is empty. Type a number";
                SceneManager.getInstance().warning(title, header, content);
                return;
            }

            if(numberTextField.getText().length() > 10) {
                String title = "Add Contact Warning";
                String header = "Cannot add a new contact";
                String content = "Number too long";
                SceneManager.getInstance().warning(title, header, content);
                return;
            }

            ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.FIND_USER_BY_NUMBER);
            ClientConnection.getInstance().getObjectOutputSendStream().writeObject(numberTextField.getText().trim());

            ResponseType response;
            response = (ResponseType) ClientConnection.getInstance().getObjectInputSendStream().readObject();


            assert response != null;
            if(response.checkType(ResponseType.CONFIRMATION)) {
                String username = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                String number = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                String name = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                String surname = (String) ClientConnection.getInstance().getObjectInputSendStream().readObject();

                if(usernamesInNewContact.contains(username)) {
                    String title = "Add Contact Warning";
                    String header = "Cannot add a new contact member";
                    String content = "Member already added to new contact";
                    SceneManager.getInstance().warning(title, header, content);
                    return;
                }

                if(username.equals(ClientStatic.getInstance().getClient().getUsername())) {
                    String title = "Add Contact Warning";
                    String header = "Cannot add a new contact member";
                    String content = "You cannot add yourself to contact. If you want make conversation available only for you, do not add anyone";
                    SceneManager.getInstance().warning(title, header, content);
                    return;
                }

                HBox hBox = new HBox();
                AnchorPane.setLeftAnchor(hBox, 0.0);
                AnchorPane.setRightAnchor(hBox, 0.0);

                hBox.getChildren().add(new Text(username + "\t" + number + "\t\t\t" + name + " " + surname));

                Button button = new Button("-");
                button.setAlignment(Pos.CENTER_RIGHT);
                button.setOnAction(e -> {
                    if(newContactUsersList.getChildren().contains(hBox)) {
                        Platform.runLater(() -> newContactUsersList.getChildren().remove(hBox));
                    }

                    usernamesInNewContact.remove(username);
                });

                button.setOnAction(e -> {
                    if(newContactUsersList.getChildren().contains(hBox)) {
                        Platform.runLater(() -> newContactUsersList.getChildren().remove(hBox));
                    }

                    usernamesInNewContact.remove(username);
                });

                hBox.getChildren().add(button);
                Platform.runLater(() -> newContactUsersList.getChildren().add(hBox));
                usernamesInNewContact.add(username);
            }
            else if(response.checkType(ResponseType.FAILURE)) {
                String title = "Add Contact Warning";
                String header = "Cannot add a new contact member";
                String content = "We cannot find the user. Check correctness of new contact number";
                SceneManager.getInstance().warning(title, header, content);
            }
        }
    }

    private void createContactLabel(Conversation conversation) {
        VBox chatBox = new VBox();

        ContactLabel contact = new ContactLabel(conversation, chatBox);
        contact.setPrefSize(260.0, 40.0);
        contact.setMinSize(260.0, 40.0);
        contact.setLayoutX(20);
        contact.setAlignment(Pos.CENTER);
        contact.setTextAlignment(TextAlignment.CENTER);
        contact.setOnMouseClicked(clickOnContactEvent);

        contactsListBox.getChildren().add(contact);
        contactLabels.add(contact);
        chatScrollPane.setContent(contact.getChatBox());
    }

    public void clearContact() {
        usernamesInNewContact.clear();
        Platform.runLater(() -> newContactUsersList.getChildren().clear());
    }

    public void clickOnUsername() {
        new Start();
    }


}