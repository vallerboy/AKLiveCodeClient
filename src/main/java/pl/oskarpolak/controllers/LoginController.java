package pl.oskarpolak.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pl.oskarpolak.AppStarter;
import pl.oskarpolak.models.*;
import pl.oskarpolak.models.services.ClientEndpoint;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


/**
 * Created by Lenovo on 23.07.2017.
 */
public class LoginController implements Initializable, ISocketConnection, IMessageObserver{

	// Login Controller witam youtube
	// HEJ!
    @FXML
    JFXTextField textNick;

    @FXML
    JFXTextField textRoom;

    @FXML
    JFXButton buttonConnect;


    @FXML
    JFXSpinner spinnerLogin;

    @FXML
    Label textInfo;


    private Stage stage;
    private ClientEndpoint clientEndpoint;
    private UserModel localUser = UserModel.getUser();

    public LoginController() {
        clientEndpoint = ClientEndpoint.getConnection();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        clientEndpoint.registerObserver(this);

        stage = AppStarter.getStage();
        createButtonActions();

    }


    private void createButtonActions(){
        buttonConnect.setOnMouseClicked(event -> {
            spinnerLogin.setVisible(true);
            if(!fieldValidator())
                return;

            connectToRoom();

        });
    }

    private void connectToRoom(){
        buttonConnect.setDisable(true);
        clientEndpoint.init(textRoom.getText(), this);
    }


    private boolean fieldValidator() {
        return true;
    }

    @Override
    public void onConnect() {
        registerUser();
    }

    @Override
    public void onError() {

    }

    @Override
    public void onNewMessage( MessageModel messageModel) {
        switch (messageModel.getMessageType()) {
            case REGISTER_RESPONSE: {
                if(messageModel.getContext().equals("nickexist")){
                    MessagesUtils.createSimpleDialog("Nazwa użytkownika", "Ta nazwa jest już zajęta w tym pokoju");
                    textNick.requestFocus();
                    buttonConnect.setDisable(false);
                    spinnerLogin.setVisible(false);
                    System.out.println("Nazwa zajęta");
                    break;
                }


                localUser.setLogged(true);

                Platform.runLater(() -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
                    Scene scene = new Scene(root, 900, 600);
                    scene.getStylesheets().add(this.getClass().getClassLoader().getResource("app.css").toExternalForm());
                    stage.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                });

                break;
            }
        }
    }

    private void registerUser() {
        localUser.setName(textNick.getText());

        System.out.println("LocalUser: " + localUser.getName());

        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.REGISTER);
        model.setContext(localUser.getName());
        clientEndpoint.sendMessage(model);
    }

}
