package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import models.FileModel;
import models.IMessageObserver;
import models.MessageModel;
import models.UserModel;
import models.components.Editor;
import models.components.FileUtils;
import models.services.ClientEndpoint;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

public class MainController implements Initializable, IMessageObserver {

    private static final Gson gson = new GsonBuilder().create();

    @FXML
    JFXListView<FileModel> fileArea;

    @FXML
    StackPane classText;


    @FXML
    JFXListView<String> userList;

    CodeArea codeArea = new CodeArea();



    private ObservableList<String> userItemList = FXCollections.observableArrayList();

    private UserModel me = UserModel.getUser();

    private UserModel activeUser;
    private ClientEndpoint endpoint = ClientEndpoint.getClientEndpoint();

    public void initialize(URL location, ResourceBundle resources) {

        me.setName(registerNickname());
        register();
        requestAllUser();

        endpoint.registerObserver(this);

        initHandlers();

        classText.getChildren().add(Editor.buildArea(codeArea));

        fileArea.setItems(me.getFiles());
        userList.setItems(userItemList);

        codeArea.setStyle("-fx-font-size: 1.5em;");
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            FileUtils.saveFile(fileArea.getSelectionModel().getSelectedItem().getPath(), newValue.getBytes());
            fileArea.getSelectionModel().getSelectedItem().setContent(newValue);
        });
    }

    private String registerNickname() {
        TextInputDialog dialog = new TextInputDialog("Oskar");
        dialog.setTitle("Ustaw swój nick");
        dialog.setHeaderText("Inni muszą Cię rozpoznać..");
        dialog.setContentText("Nick:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            return result.get();
        }
        return null;
    }


    private void initHandlers() {
        fileArea.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });

        fileArea.setOnDragDropped(event -> {
            if (event.getDragboard().hasFiles()) {
                for (File file : event.getDragboard().getFiles()) {

                    if(file.isDirectory()){
                        listFiles(file);
                        continue;
                    }

                    if(!validateName(file)) {
                        continue;
                    }

                    me.addFile((new FileModel(file.getName(), file.toPath())));
                }
            }
        });

        fileArea.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileModel>() {
            @Override
            public void changed(ObservableValue<? extends FileModel> observable, FileModel oldValue, FileModel newValue) {
                codeArea.replaceText(FileUtils.readAllFile(newValue.getPath()));
            }
        });

    }



    private void listFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                if(!validateName(file)) {
                    continue;
                }
                me.addFile((new FileModel(file.getName(), file.toPath())));
            } else if (file.isDirectory()) {
                listFiles(file);
            }
        }
    }

    private boolean validateName(File f){
        return f.getName().endsWith(".txt") || f.getName().endsWith(".html") || f.getName().endsWith(".java") || f.getName().endsWith(".properties") || f.getName().endsWith(".gitignore") || f.getName().endsWith(".fxml") || f.getName().endsWith(".css") || f.getName().endsWith(".js");
    }

    @Override
    public void onNewMessage(ByteBuffer byteBuffer) {
        Type type = new TypeToken<MessageModel>(){}.getType();
        MessageModel messageModel = gson.fromJson(new String(byteBuffer.array()),  type);


         switch (messageModel.getMessageType()){
             case DOWNLOAD_REQUEST:{
                    String toWho = messageModel.getToWho();
                    sendProjectTo(toWho);
                    break;
             }
             case JOIN:{
                 Platform.runLater(() -> {
                     userItemList.add(messageModel.getContext());
                 });
                    break;
             }
             case REQUEST_ALL_USER:{
                 Type typeUsers = new TypeToken<Collection<String>>(){}.getType();
                 System.out.println(messageModel.getContext());
                 userItemList.addAll((Collection<? extends String>) gson.fromJson(messageModel.getContext(), typeUsers));

                 break;
             }
             case REGISTER_RESPONSE:{
                 System.out.println("Response: "  + messageModel.getContext());
                 break;
             }

         }
    }

    private void sendMessage(MessageModel model){
        String message = gson.toJson(model);
        try {
            endpoint.getSession().getBasicRemote().sendBinary(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendProjectTo(String name){
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.DOWNLOAD_RESPONSE);
        model.setToWho(name);
        model.setContext(gson.toJson(me));
        sendMessage(model);
    }


    private void register() {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.REGISTER);
        model.setContext(me.getName());
        sendMessage(model);
    }

    private void requestAllUser() {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.REQUEST_ALL_USER);
        sendMessage(model);
    }
}
