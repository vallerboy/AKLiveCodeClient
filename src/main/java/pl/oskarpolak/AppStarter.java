package pl.oskarpolak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppStarter extends Application {


    private static Stage stage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("login.fxml"));
        primaryStage.setTitle("LiveCode");
        Scene scene = new Scene(root, 900, 600);
        // scene.getStylesheets().add(this.getClass().getClassLoader().getResource("app.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static Stage getStage(){
        return stage;
    }




    public static void main(String[] args) {
        launch(args);
    }
}
