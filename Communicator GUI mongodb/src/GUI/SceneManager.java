package GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private static SceneManager instance = null;

    private Stage primaryStage;
    private List<Scene> scenes;

    public enum SceneName {
        LOGIN,
        REGISTER,
        COMMUNICATOR
    }

    private SceneManager(){}

    private Scene getScene(SceneName name){
        return scenes.get(name.ordinal());
    }

    public void setScenes() throws IOException {

        scenes = new ArrayList<>();

        Parent loginRoot, registerRoot, communicatorRoot;
        loginRoot = FXMLLoader.load(getClass().getResource("View/loginView.fxml"));
        Scene startScene = new Scene(loginRoot, 800, 600);

        registerRoot = FXMLLoader.load(getClass().getResource("View/registerView.fxml"));
        Scene quizScene = new Scene(registerRoot, 800, 600);

        communicatorRoot = FXMLLoader.load(getClass().getResource("View/communicatorView.fxml"));
        Scene communicatorScene = new Scene(communicatorRoot, 800, 600);

        scenes.add(startScene);
        scenes.add(quizScene);
        scenes.add(communicatorScene);
    }

    public static SceneManager getInstance() {
        if(instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }
    public void start() {
        primaryStage.setTitle("Login to communicator");
        primaryStage.setScene(getScene(SceneName.LOGIN));
        primaryStage.show();
        primaryStage.setOnCloseRequest(evt -> System.exit(0));
    }
    public void setScene(SceneName name) {
        primaryStage.setScene(getScene(name));
    }

    public void information(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);    //do poprawy
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void warning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


