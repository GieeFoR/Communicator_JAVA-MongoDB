package GUI.Controllers;

import GUI.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import Client.ClientConnection;
import Client.ClientStatic;
import Common.*;

public class LoginController {
    ClientConnection clientConnection = null;

    @FXML
    private TextField loginTextField;
    @FXML
    private PasswordField passwordTextField;

    public void initialize() {
        clientConnection = ClientConnection.getInstance();
        ClientStatic.getInstance();
    }

    public void onSignupAction() {
        SceneManager.getInstance().setScene(SceneManager.SceneName.REGISTER);
    }

    public void onLoginAction() {
        if(loginTextField.getText().length() > 32) {
            String title = "LogIn Warning";
            String header = "LogIn failed";
            String content = "Your username is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(passwordTextField.getText().length() > 64) {
            String title = "LogIn Warning";
            String header = "LogIn failed";
            String content = "Your password is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        try {
            clientConnection.getObjectOutputSendStream().writeObject(InformationType.LOGIN);
            clientConnection.getObjectOutputSendStream().writeObject(loginTextField.getText());
            clientConnection.getObjectOutputSendStream().writeObject(passwordTextField.getText());
        } catch (IOException e) {
            System.out.println("The message could not be sent");
            //maybe close connection and try to connect again?
        }

        ResponseType response = null;
        try {
            response = (ResponseType) clientConnection.getObjectInputSendStream().readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        assert response != null;
        if (response.checkType(ResponseType.CONFIRMATION)) {
            Client client = null;
            try {
                client = (Client) clientConnection.getObjectInputSendStream().readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            assert client != null;
            ClientStatic.getInstance().setClient(client);
            SceneManager.getInstance().setScene(SceneManager.SceneName.COMMUNICATOR);

            String title = "LogIn Information";
            String header = "LogIn completed successfully";
            String content = "You are logged in to your account";
            SceneManager.getInstance().information(title, header, content);
        } else if (response.checkType(ResponseType.ALREADY_LOGGED_IN)) {
            String title = "LogIn Warning";
            String header = "LogIn failed";
            String content = "Cannot log in to your account because you are already logged in";
            SceneManager.getInstance().warning(title, header, content);
        } else if (response.checkType(ResponseType.WRONG_USERNAME_PASSWORD)) {
            String title = "LogIn Warning";
            String header = "LogIn failed";
            String content = "Cannot log in to your account because of wrong username or password. Check your username and password";
            SceneManager.getInstance().warning(title, header, content);
        } else {
            System.out.println("ERROR #102");
            System.exit(-1);
        }
    }
}