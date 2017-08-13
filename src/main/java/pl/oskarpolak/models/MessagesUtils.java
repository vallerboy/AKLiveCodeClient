package pl.oskarpolak.models;

import javafx.scene.control.Alert;

/**
 * Created by Lenovo on 13.08.2017.
 */
public class MessagesUtils {
    public static void createSimpleDialog(String title, String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Uwaga");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
