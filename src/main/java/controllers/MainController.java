package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable, IMessageObserver {

    private static final Gson gson = new GsonBuilder().create();

    @FXML
    private JFXListView<FileModel> fileArea;

    @FXML
    private StackPane classText;


    @FXML
    private JFXListView<String> userList;

    private CodeArea codeArea = new CodeArea();

    private ObservableList<String> userItemList = FXCollections.observableArrayList();

    private UserModel localUser = UserModel.getUser();

    private FileModel activeFile = null;
    private ArrayList<FileModel> otherUserFiles;
    private String currentSession;

    private String observe;
    private int cursorPosition = 0;


    private ClientEndpoint endpoint = new ClientEndpoint();

    public void initialize(URL location, ResourceBundle resources) {
        endpoint.init();

        otherUserFiles = new ArrayList<>();

        localUser.setName(registerNickname());
        register();
        requestAllUser();

        endpoint.registerObserver(this);

        initHandlers();

        classText.getChildren().add(Editor.buildArea(codeArea));

        fileArea.setItems(FXCollections.observableArrayList(localUser.getFiles()));
        userItemList.add("Ty to " + localUser.getName());
        userList.setItems(userItemList);

        codeArea.setStyle("-fx-font-size: 1.5em;");
        codeArea.setOnKeyTyped(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                    fileArea.getSelectionModel().getSelectedItem().setContent(codeArea.getText());
                    cursorPosition = codeArea.getCaretPosition();
                    if(observe != null && observe.equals(localUser.getName())) {
                    FileUtils.saveFile(fileArea.getSelectionModel().getSelectedItem().getPath(), codeArea.getText().getBytes());
                    }
                    updateProject(fileArea.getSelectionModel().getSelectedItem());
                System.out.println("Położenie kursora: " + cursorPosition);
            }
        });
        //todo pozycja kursora musi się odswieżać na swojej pozycji
        codeArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                System.out.println("Pozycja v2: " + cursorPosition);
                codeArea.displaceCaret(cursorPosition);
            }
        });

    }

    private String registerNickname() {
        TextInputDialog dialog = new TextInputDialog("Oskar");
        dialog.setTitle("Ustaw swój nick");
        dialog.setHeaderText("Inni muszą Cię rozpoznać..");
        dialog.setContentText("Nick:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
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

                    if (file.isDirectory()) {
                        listFiles(file);
                        continue;
                    }

                    if (!validateName(file)) {
                        continue;
                    }

                    localUser.addFile((new FileModel(file.getName(), file.toPath())));
                    fileArea.setItems(FXCollections.observableArrayList(localUser.getFiles()));
                }
            }
        });

        fileArea.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileModel>() {
            @Override
            public void changed(ObservableValue<? extends FileModel> observable, FileModel oldValue, FileModel newValue) {
                if(newValue != null) {
                    activeFile = newValue;
                    System.out.println("Nowy aktywny plik: " + newValue.getName());
                    codeArea.replaceText(newValue.getContent());
                }
            }
        });

        userList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null){
                observe = newValue;
            }
            if (!observe.equals(localUser.getName())) {
                getProjectFrom(observe);
            } else {
                fileArea.setItems(FXCollections.observableArrayList(localUser.getFiles()));
            }
        });

    }


    private void listFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                if (!validateName(file)) {
                    continue;
                }
                localUser.addFile((new FileModel(file.getName(), file.toPath())));
            } else if (file.isDirectory()) {
                listFiles(file);
            }
        }
    }

    private boolean validateName(File f) {
        return f.getName().endsWith(".txt") || f.getName().endsWith(".html") || f.getName().endsWith(".java") || f.getName().endsWith(".properties") || f.getName().endsWith(".gitignore") || f.getName().endsWith(".fxml") || f.getName().endsWith(".css") || f.getName().endsWith(".js");
    }


    @Override
    public void onNewMessage(ByteBuffer byteBuffer) {
        Type type = new TypeToken<MessageModel>() {
        }.getType();
        MessageModel messageModel = gson.fromJson(new String(byteBuffer.array()), type);


        switch (messageModel.getMessageType()) {
            case DOWNLOAD_REQUEST: {
                String toWho = messageModel.getToWho();
                currentSession = toWho;
                sendProjectTo(toWho);
                break;
            }
            case JOIN: {
                Platform.runLater(() -> {
                    userItemList.add(messageModel.getContext());
                });
                break;
            }
            case REQUEST_ALL_USER: {
                Type typeUsers = new TypeToken<Collection<String>>() {
                }.getType();
                System.out.println(messageModel.getContext());
                userItemList.addAll((Collection<? extends String>) gson.fromJson(messageModel.getContext(), typeUsers));

                break;
            }
            case DOWNLOAD_RESPONSE: {
                Type typeUsers = new TypeToken<UserModel>() {
                }.getType();
                UserModel model = gson.fromJson(messageModel.getContext(), typeUsers);


                otherUserFiles = model.getFiles();

                Platform.runLater(() -> {
                    fileArea.getSelectionModel().clearSelection();
                    fileArea.setItems(FXCollections.observableArrayList(otherUserFiles));
                });

                break;
            }
            case REGISTER_RESPONSE: {
                System.out.println("Wiadomość od serwera: " + messageModel.getContext());
                break;
            }

            case UPDATING: {
                if(observe != null && observe.equals(localUser.getName())){
                    break;
                }
                FileModel fileModel = gson.fromJson(messageModel.getContext(), FileModel.class);
                updateFile(fileModel);

                Platform.runLater(() ->{
                    if(fileArea.getSelectionModel().getSelectedItem() != null &&
                            fileArea.getSelectionModel().getSelectedItem().equals(fileModel)) {
                        codeArea.replaceText(fileModel.getContent());

                    }
                });

                System.out.println("Update zapisany");
                break;
            }



        }
    }

    private void sendMessage(MessageModel model) {
        String message = gson.toJson(model);
        try {
            endpoint.getSession().getBasicRemote().sendBinary(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendProjectTo(String name) {
        System.out.println("Name: " + name);
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.DOWNLOAD_RESPONSE);
        model.setToWho(name);

        Type listType = new TypeToken<UserModel>() {
        }.getType();
        model.setContext(gson.toJson(localUser, listType));
        sendMessage(model);

    }

    //todo dupa dupa dupa, tego nie powinno tu być
    private void register() {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.REGISTER);
        model.setContext(localUser.getName());
        sendMessage(model);
    }

    private void requestAllUser() {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.REQUEST_ALL_USER);
        sendMessage(model);
    }

    private void getProjectFrom(String newValue) {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.DOWNLOAD_REQUEST);
        model.setToWho(newValue);
        sendMessage(model);
        System.out.println("Wysłałem zapytanie o pliczki");
    }

    private void updateProject(FileModel fileModel) {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.UPDATING);
        model.setContext(gson.toJson(fileModel));
        sendMessage(model);
    }

    private void updateFile(FileModel fileModel) {
        otherUserFiles.stream()
                .filter(s -> s.getPath().equals(fileModel.getPath()))
                .findAny()
                .ifPresent(s -> {
                    s.setContent(fileModel.getContent());
                    if(observe != null && observe.equals(localUser.getName())) {
                        FileUtils.saveFile(fileArea.getSelectionModel().getSelectedItem().getPath(), fileModel.getContent().getBytes());
                    }
                });
    }


}
