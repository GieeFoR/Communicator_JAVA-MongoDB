package GUI.Controllers;

import GUI.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Date;

import Client.ClientConnection;
import Common.*;

public class RegisterController {

    ClientConnection clientConnection = null;

    @FXML
    private TextField loginTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordTextField1;

    @FXML
    private PasswordField passwordTextField2;

    @FXML
    public TextField nameTextField;

    @FXML
    public TextField surnameTextField;

    public void initialize() {
        clientConnection = ClientConnection.getInstance();
    }

    public void onSignupAction() {
        if(loginTextField.getText().length() > 32) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your username is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(loginTextField.getText().trim().equals("")) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your username field is empty";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(emailTextField.getText().length() > 32) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your email is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(emailTextField.getText().trim().equals("")) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your email field is empty";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(passwordTextField1.getText().trim().equals("") || passwordTextField2.getText().trim().equals("")) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your password field is empty";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(!passwordTextField1.getText().equals(passwordTextField2.getText())) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your passwords are different";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(passwordTextField1.getText().length() > 64) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your password is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(nameTextField.getText().trim().equals("")) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your name field is empty";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(nameTextField.getText().length() > 32) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your name is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(surnameTextField.getText().trim().equals("")) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your surname field is empty";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        if(surnameTextField.getText().length() > 32) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Your surname is too long";
            SceneManager.getInstance().warning(title, header, content);
            return;
        }

        try {
            clientConnection.getObjectOutputSendStream().writeObject(InformationType.REGISTER);

            Account account = new Account(loginTextField.getText(), passwordTextField1.getText(), emailTextField.getText(), new Date());
            clientConnection.getObjectOutputSendStream().writeObject(account);
            clientConnection.getObjectOutputSendStream().writeObject(nameTextField.getText());
            clientConnection.getObjectOutputSendStream().writeObject(surnameTextField.getText());

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
        if(response.checkType(ResponseType.CONFIRMATION)) {
            SceneManager.getInstance().setScene(SceneManager.SceneName.LOGIN);

            String title = "SignUp Information";
            String header = "Registration completed successfully";
            String content = "Your account was correctly created. Now you can log in to your new account";
            SceneManager.getInstance().information(title, header, content);
        }
        else if(response.checkType(ResponseType.LOGIN_TAKEN)) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Username already in use";
            SceneManager.getInstance().warning(title, header, content);
        }
        else if(response.checkType(ResponseType.EMAIL_TAKEN)) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "Email already in use";
            SceneManager.getInstance().warning(title, header, content);
        }
        else if(response.checkType(ResponseType.FAILURE)) {
            String title = "SignUp Warning";
            String header = "Registration failed";
            String content = "";
            SceneManager.getInstance().warning(title, header, content);
        }
        else {
            System.out.println("ERROR #101");
            System.exit(-1);
        }
    }

    public void onLoginAction() {
        SceneManager.getInstance().setScene(SceneManager.SceneName.LOGIN);
    }
}
