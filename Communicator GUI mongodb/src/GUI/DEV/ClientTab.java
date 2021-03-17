package GUI.DEV;

import Client.ClientConnection;
import Common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ClientTab {
    Tab clientTab;
    private VBox vBox = Start.vBox;

    public ClientTab(Tab tab) {
        this.clientTab = tab;
        readCollectionOnSelect();

        ComboBox<String> comboBox = new ComboBox<>();

        String all = "Wybierz wszystkich użytkowników istniejących w bazie danych";
        String clientWithNumber = "Wybierz użytkownika z określonym numerem";
        String clientWithNameSurname = "Wybierz użytkowników o podanym imieniu i nazwisku";
        String clientsInConv = "Wybierz użytkowników należących do określonej konwersacji";
        String clientLast = "Wybierz najnowszego użytkownika";
        String clientDate = "Wybierz użytkowników którzy założyli konto w podanym dniu";

        comboBox.getItems().addAll(all, clientWithNumber, clientWithNameSurname, clientsInConv, clientLast, clientDate);
        comboBox.setValue(all);

        TextField textField1 = new TextField();
        textField1.setVisible(false);

        TextField textField2 = new TextField();
        textField2.setVisible(false);


        EventHandler<ActionEvent> event =
                e -> {
                    textField1.clear();
                    textField2.clear();
                    if(comboBox.getValue().equals(clientWithNumber)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź numer użytkownika");
                        textField2.setVisible(false);
                    }
                    else if(comboBox.getValue().equals(clientWithNameSurname)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź imię użytkownika");
                        textField2.setVisible(true);
                        textField2.setPromptText("Wprowadź nazwisko użytkownika");
                    }
                    else if(comboBox.getValue().equals(clientsInConv)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź id konwersacji");
                        textField2.setVisible(false);
                    }
                    else if(comboBox.getValue().equals(clientLast)) {
                        textField1.setVisible(false);
                        textField2.setVisible(false);
                    }
                    else if(comboBox.getValue().equals(clientDate)) {
                        textField1.setVisible(true);
                        textField1.setPromptText("Wprowadź datę w formacie YYYY-mm-dd");
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
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENTS);
                    List<Client> clients =  (List<Client>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(clients);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(clientWithNumber)) {
                if(textField1.getText().equals("")) {
                    return;
                }
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENT);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(Integer.parseInt(textField1.getText()));
                    Client client =  (Client) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(client);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(clientWithNameSurname)) {
                if(textField1.getText().equals("")) {
                    return;
                }
                if(textField2.getText().equals("")) {
                    return;
                }

                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENT_BY_NAME);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(textField1.getText());
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(textField2.getText());
                    Client client = (Client) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(client);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(clientsInConv)) {
                if(textField1.getText().equals("")) {
                    return;
                }

                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENTS_CONV);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(new ObjectId(textField1.getText()));
                    List<Client> clients = (List<Client>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(clients);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(clientLast)) {
                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENT_LAST);
                    Client client = (Client) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(client);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(comboBox.getValue().equals(clientDate)) {
                if(textField1.getText().equals("")) {
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENT_BY_DATE);
                    ClientConnection.getInstance().getObjectOutputSendStream().writeObject(format.parse(textField1.getText()));
                    List<Client> clients = (List<Client>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                    showCollection(clients);
                } catch (IOException | ClassNotFoundException | ParseException e) {
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
        clientTab.setOnSelectionChanged(event -> {
            try {
                ClientConnection.getInstance().getObjectOutputSendStream().writeObject(InformationType.DB_GET_CLIENTS);
                List<Client> clients =  (List<Client>) ClientConnection.getInstance().getObjectInputSendStream().readObject();
                showCollection(clients);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void showCollection(List<Client> clients) {
        vBox.getChildren().clear();
        if(clients == null) {
            return;
        }

        for (Client c: clients) {
            Text output = new Text(c.toString());
            output.setFont(new Font(13));
            Text separator = new Text("=========================================================================");
            separator.setFill(Color.GREEN);
            vBox.getChildren().addAll(output, separator);
        }
    }

    private void showCollection(Client client) {
        vBox.getChildren().clear();
        if(client == null) {
            return;
        }

        Text output = new Text(client.toString());
        output.setFont(new Font(13));
        Text separator = new Text("===========================================================================");
        separator.setFill(Color.GREEN);
        vBox.getChildren().addAll(output, separator);

    }
}
