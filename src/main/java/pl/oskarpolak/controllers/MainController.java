package pl.oskarpolak.controllers;

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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import pl.oskarpolak.models.FileModel;
import pl.oskarpolak.models.IMessageObserver;
import pl.oskarpolak.models.MessageModel;
import pl.oskarpolak.models.UserModel;
import pl.oskarpolak.models.components.Editor;
import pl.oskarpolak.models.components.FileUtils;
import pl.oskarpolak.models.services.ClientEndpoint;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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


    private ClientEndpoint endpoint = ClientEndpoint.getConnection();

    public void initialize(URL location, ResourceBundle resources) {


        otherUserFiles = new ArrayList<>();

        requestAllUser();



        userList.getSelectionModel().select(localUser.getName());
        observe = localUser.getName();

        endpoint.registerObserver(this);

        initHandlers();

        classText.getChildren().add(Editor.buildArea(codeArea));

        fileArea.setItems(FXCollections.observableArrayList(localUser.getFiles()));
        userList.setItems(userItemList);

        codeArea.setStyle("-fx-font-size: 1.5em;");
        codeArea.setOnKeyTyped(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                    fileArea.getSelectionModel().getSelectedItem().setContent(codeArea.getText());
                    if(observe != null && observe.equals(localUser.getName())) {
                    FileUtils.saveFile(fileArea.getSelectionModel().getSelectedItem().getPath(), codeArea.getText().getBytes());
                    }
                    fileArea.getSelectionModel().getSelectedItem().setCursor(codeArea.getCaretPosition());
                    updateProject(fileArea.getSelectionModel().getSelectedItem());
                    System.out.println("Wysyłam update do obserwatorów");

            }
        });
        //todo pozycja kursora musi się odswieżać na swojej pozycji
        codeArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                //codeArea.displaceCaret(cursorPosition);
            }
        });

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
    public void onNewMessage(MessageModel messageModel) {

        switch (messageModel.getMessageType()) {
            case DOWNLOAD_REQUEST: {
                String toWho = messageModel.getToWho();
                currentSession = toWho;
                sendProjectTo(toWho);
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

            case DISCONNECT: {
                Platform.runLater(() ->  userItemList.remove(messageModel.getToWho()));
                break;
            }
            case JOIN: {
                Platform.runLater(() -> {
                    userItemList.add(messageModel.getContext());
                });
                break;
            }
            case UPDATING: {
                System.out.println("Przychodzi udpdate od kogoś");
//                if(observe != null && observe.equals(localUser.getName())){
//                    break;
//                }
                FileModel fileModel = gson.fromJson(messageModel.getContext(), FileModel.class);
                updateFile(fileModel);

                Platform.runLater(() ->{
                    if(fileArea.getSelectionModel().getSelectedItem() != null &&
                            fileArea.getSelectionModel().getSelectedItem().equals(fileModel)) {
                        codeArea.replaceText(fileModel.getContent());
                        codeArea.displaceCaret(fileModel.getCursor());
                        System.out.println("Ustawiam kursor na " + fileModel.getCursor());
                    }else{
                        for (FileModel model : localUser.getFiles()) {
                            if(model.equals(fileModel)){
                                model.setContent(fileModel.getContent());
                            }
                        }
                        System.out.println("Jakiś nieaktywny user edytuje plik");
                    }
                });

                System.out.println("Update zapisany");
                break;
            }



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
        endpoint.sendMessage(model);

    }


    private void requestAllUser() {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.REQUEST_ALL_USER);
        endpoint.sendMessage(model);
    }

    private void getProjectFrom(String newValue) {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.DOWNLOAD_REQUEST);
        model.setToWho(newValue);
        endpoint.sendMessage(model);
        System.out.println("Wysłałem zapytanie o pliczki");
    }

    private void updateProject(FileModel fileModel) {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.UPDATING);
        model.setContext(gson.toJson(fileModel));

        endpoint.sendMessage(model);
    }

    private void disconnect() {
        MessageModel model = new MessageModel();
        model.setMessageType(MessageModel.MessageType.DISCONNECT);
        model.setToWho(localUser.getName());

        endpoint.sendMessage(model);
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
