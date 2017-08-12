package models;

import com.google.gson.annotations.SerializedName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by Lenovo on 23.07.2017.
 */
public class UserModel implements Serializable{

    private transient static final UserModel userModel = new UserModel();
    public static UserModel getUser() {
        return userModel;
    }


    private String name;
    private ArrayList<FileModel> files;

    public UserModel(){
        files =  new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public void addFile(FileModel model) {
        model.setAuthor(name);
        files.add(model);
    }

    public ArrayList<FileModel> getFiles() {
        return files;
    }

    public void setName(String name) {
        this.name = name;
    }


}
