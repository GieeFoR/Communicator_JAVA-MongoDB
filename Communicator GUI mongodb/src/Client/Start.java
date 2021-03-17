package Client;

import GUI.SceneManager;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Start extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        ClientConnection clientConnection = ClientConnection.getInstance();
        try {
            clientConnection.connect("localhost", 4999);
        }catch (IOException e) {
            System.out.println(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection error");
            alert.setHeaderText("Cannot connect the client to the server");
            alert.setContentText("Check your network connection or try again later");
            alert.showAndWait();
            System.exit(-1);
        }

        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(stage);
        sceneManager.setScenes();
        sceneManager.start();
    }
}