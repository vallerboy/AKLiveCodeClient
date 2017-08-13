package pl.oskarpolak.models;

import java.io.Serializable;
import java.util.ArrayList;

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
    private transient boolean isLogged;

    public UserModel(){
        files =  new ArrayList<>();
    }





    public boolean isLoged() {
        return isLogged;
    }

    public void setLogged(boolean isLogged) {
        isLogged = isLogged;
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
