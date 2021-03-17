package GUI.DEV;

import Client.ClientConnection;
import Common.Conversation;
import Common.InformationType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

public class ConversationTab {
    Tab conversationTab;
    private VBox vBox = Start.vBox;

    public ConversationTab(Tab tab) {
        this.conversationTab = tab;
        readCollectionOnSelect();

        ComboBox<String> comboBox = new ComboBox<>();

        String all = "Wybierz wszystkie konwersacje istniejące w bazie danych";
        String convsWithUser = "Wybierz konwersacje, do których należy użytkownik o podanym numerze";
        String convsCreatedByUser = "Wybierz konwersacje założone przez użytkownika o podanym numerze";

        comboBox.getItems().addAll(all, convsWithUser, convsCreatedByUser);
        comboBox.setValue(all);

        TextField textField = new TextField();
        textField.setVisible(false);

        EventHandler<ActionEvent> event =
                e -> {
                    textField.clear();
                    if(comboBox.getValue().equals(convsWithUser) || comboBox.getValue().equals(convsCreatedByUser)) {
                        textField.setVisible(true);
                        textField.setPromptText("Wprowadź numer użytkownika");
                    }
                    else {
                        textField.setVisible(false);
                    }
                };

        // Set on action
        comboBox.setOnAction(event);

        Button button = new Button("Wyszukaj");

        button.setOnMouseClicked(me -> {
            if(comboBox.getValue().equals(all)) {
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CONVS);
                    List<Conversation> conversations =  (List<Conversation>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(conversations);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(convsWithUser)) {
                if(textField.getText().equals("")) {
                    return;
                }
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CONV_CLIENT);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(Integer.parseInt(textField.getText()));
                    List<Conversation> conversations =  (List<Conversation>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(conversations);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(convsCreatedByUser)) {
                if(textField.getText().equals("")) {
                    return;
                }
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CONV_CLIENT_FOUNDED);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(Integer.parseInt(textField.getText()));
                    List<Conversation> conversations =  (List<Conversation>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(conversations);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        VBox vBox = new VBox();
        vBox.setSpacing(5.0);
        vBox.setPadding(new Insets(0,80,0,80));
        vBox.getChildren().addAll(comboBox, textField, button);
        vBox.setAlignment(Pos.CENTER);
        tab.setContent(vBox);
    }

    private void readCollectionOnSelect() {
        conversationTab.setOnSelectionChanged(event -> {
            try {
                ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CONVS);
                List<Conversation> conversations =  (List<Conversation>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                showCollection(conversations);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void showCollection(List<Conversation> conversations) {
        vBox.getChildren().clear();
        if(conversations == null) {
            return;
        }

        for (Conversation c: conversations) {
            Text output = new Text(c.toString());
            output.setFont(new Font(13));
            Text separator = new Text("=================================================");
            separator.setFill(Color.GREEN);
            vBox.getChildren().addAll(output, separator);
        }
    }
}
