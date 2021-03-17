package GUI.DEV;

import Client.ClientConnection;
import Common.InformationType;
import Common.Message;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MessageTab {
    Tab messageTab;
    private VBox vBox = Start.vBox;

    public MessageTab(Tab tab) {
        this.messageTab = tab;
        readCollectionOnSelect();

        ComboBox<String> comboBox = new ComboBox<>();

        String all = "Wybierz wszystkie wiadomości istniejące w bazie danych";
        String messInConv = "Wybierz wiadomości w podanej konwersacji";
        String messClientWithPattern = "Wybierz wiadomości podanego użytkownika zawierające podaną treść";
        String messClientDate = "Wybierz wiadomości podanego użytkownika wysłanych w danym dniu";
        String messClientLast = "Wybierz ostatnią wysłaną wiadomość podanego użytkownika";

        comboBox.getItems().addAll(all, messInConv, messClientWithPattern, messClientDate, messClientLast);
        comboBox.setValue(all);

        TextField textField1 = new TextField();
        textField1.setVisible(false);

        TextField textField2 = new TextField();
        textField2.setVisible(false);

        EventHandler<ActionEvent> event =
                e -> {
                    textField1.clear();
                    textField2.clear();
                    if(comboBox.getValue().equals(messInConv)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź id konwersacji");
                        textField2.setVisible(false);
                    }
                    else if(comboBox.getValue().equals(messClientWithPattern)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź numer użytkownika");
                        textField2.setVisible(true);
                        textField2.setPromptText("Wprowadź treść");
                    }
                    else if(comboBox.getValue().equals(messClientDate)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź numer użytkownika");
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź datę w formacie YYYY-mm-dd");
                    }
                    else if(comboBox.getValue().equals(messClientLast)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź numer użytkownika");
                        textField2.setVisible(false);
                    }
                    else {
                        textField1.setVisible(false);
                        textField2.setVisible(false);
                    }
                };

        // Set on action
        comboBox.setOnAction(event);

        Button button = new Button("Wyszukaj");

        button.setOnMouseClicked(me -> {
            if(comboBox.getValue().equals(all)) {
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_MESS);
                    List<Message> messages =  (List<Message>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(messages);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(messInConv)) {
                if(textField1.getText().equals("")) {
                    return;
                }
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_MESS_CONV);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(new ObjectId(textField1.getText()));
                    List<Message> messages =  (List<Message>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(messages);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(messClientWithPattern)) {
                if(textField1.getText().equals("")) {
                    return;
                }
                if(textField2.getText().equals("")) {
                    return;
                }

                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_MESS_BY_CLIENT_WITH_REGEX);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(Integer.parseInt(textField1.getText()));
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(textField2.getText());
                    List<Message> messages =  (List<Message>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(messages);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(messClientDate)) {
                if(textField1.getText().equals("")) {
                    return;
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_MESS_BY_CLIENT_AND_DATE);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(Integer.parseInt(textField1.getText()));
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(format.parse(textField2.getText()));
                    List<Message> messages =  (List<Message>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(messages);
                } catch (IOException | ClassNotFoundException | ParseException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(messClientLast)) {
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_MESS_LAST);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(Integer.parseInt(textField1.getText()));
                    Message message = (Message) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(message);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        VBox vBox = new VBox();
        vBox.setSpacing(5.0);
        vBox.setPadding(new Insets(0,80,0,80));
        vBox.getChildren().addAll(comboBox, textField1, textField2, button);
        vBox.setAlignment(Pos.CENTER);
        tab.setContent(vBox);
    }

    private void readCollectionOnSelect() {
        messageTab.setOnSelectionChanged(event -> {
            try {
                ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_MESS);
                List<Message> messages =  (List<Message>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                showCollection(messages);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void showCollection(List<Message> messages) {
        vBox.getChildren().clear();
        if(messages == null) {
            return;
        }

        for (Message m: messages) {
            Text output = new Text(m.toString());
            Text separator = new Text("=================================================");
            separator.setFill(Color.GREEN);
            vBox.getChildren().addAll(output, separator);
        }
    }

    private void showCollection(Message message) {
        vBox.getChildren().clear();
        if(message == null) {
            return;
        }

        Text output = new Text(message.toString());
        Text separator = new Text("=================================================");
        separator.setFill(Color.GREEN);
        vBox.getChildren().addAll(output, separator);

    }
}
