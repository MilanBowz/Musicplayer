package bowzgore.milan.musicfolderplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.io.IOException;

public class MusicfolderplayerApplication extends Application{

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MusicfolderplayerApplication.class.getResource("Scene.fxml"));
        Image image = new Image("Icon.png");

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("MFP (Music Folder Player)");
        stage.getIcons().add(image);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private static void simulateDynamicContentChange(Notifications notification) {
        // Simulate changing content after 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}