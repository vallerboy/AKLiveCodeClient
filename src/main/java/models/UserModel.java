package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Observable;

/**
 * Created by Lenovo on 23.07.2017.
 */
public class UserModel {

    private static final UserModel userModel = new UserModel();
    public static UserModel getUser() {
        return userModel;
    }


    private String name;
    private ObservableList<FileModel> files;

    private UserModel(){
        files =  FXCollections.observableArrayList();
    }


    public String getName() {
        return name;
    }

    public void addFile(FileModel model) {
        model.setAuthor(name);
        files.add(model);
    }

    public ObservableList<FileModel> getFiles() {
        return files;
    }

    public void setName(String name) {
        this.name = name;
    }
}
