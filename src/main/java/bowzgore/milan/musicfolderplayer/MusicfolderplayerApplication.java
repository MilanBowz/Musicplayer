package bowzgore.milan.musicfolderplayer;

import bowzgore.milan.musicfolderplayer.rest.UI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class MusicfolderplayerApplication extends Application{

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MusicfolderplayerApplication.class.getResource("Scene.fxml"));
        Image image = new Image("Icon.png");


        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setTitle("MFP (Music Folder Player)");
        stage.getIcons().add(image);
        stage.setResizable(false);
        stage.setScene(scene);
        UI controller = fxmlLoader.getController();

        stage.setOnCloseRequest(event -> {
            controller.StopApp();
            Platform.exit();
            System.exit(0);
        });
        stage.show();

        scene.setOnKeyPressed(controller::handleKeyPress);

    }



    public static void main(String[] args) {
        launch();
    }
}