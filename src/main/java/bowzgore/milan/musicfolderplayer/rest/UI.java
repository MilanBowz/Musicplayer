package bowzgore.milan.musicfolderplayer.rest;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.util.*;
import java.util.Timer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.event.EventHandler;
import org.jaudiotagger.tag.datatype.Artwork;


public class UI implements Initializable {

    @FXML
    public Label folderLabel;
    @FXML
    private Pane pane;
    @FXML
    private Label songLabel;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private ImageView imageView;

    @FXML
    private TableView<String> stringRecordsTable;

    public Sound music ;

    public int songPlaying = 0;

    private Timer timer;
    private TimerTask task;

    ImageView playIcon = new ImageView(new Image("pause.png"));



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up the table columns
        TableColumn<String, String> recordsColumn = new TableColumn<>("Records");
        recordsColumn.setPrefWidth(400);
        recordsColumn.setCellValueFactory(new PropertyValueFactory<>(""));

        // Set up the table columns
        recordsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));

        // Add columns to the table
        stringRecordsTable.getColumns().add(recordsColumn);

        playIcon.setFitHeight(40);
        playIcon.setFitWidth(40);
        playIcon.setPreserveRatio(true);
        /*
        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }*/

        //Image image = new Image("src/main/java/bowzgore/icon.png");
        //imageView.setImage(image);

        /*speedBox.setOnAction(this::changeSpeed);*/
        beginTimer();

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                music.changeVolume(volumeSlider.getValue());
            }

        });
        // Add an event handler to the ProgressBar for seeking
        songProgressBar.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent mouseEvent) {
                double totalDuration = music.getEnd();
                double seekTime = mouseEvent.getX() / songProgressBar.getWidth() * totalDuration;
                music.setCurrent(seekTime);
            }
        });
        // Set up the table
        stringRecordsTable.setRowFactory(tv -> new TableRow<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // Set the background color for a specific row

                if (getIndex() == songPlaying) {
                    setStyle("-fx-background-color: lightblue;-fx-color: black;");
                } else {
                    setStyle("");
                }
            }
            {
                // Add an event handler to the row
                setOnMouseClicked(event -> {
                    if (!isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                        // Update the currently playing song based on the clicked row
                        int clickedIndex = getIndex();
                        updateCurrentlyPlaying(clickedIndex);
                        updateUIWithSound();
                    }
                });
            }
        });
        if(!Objects.equals(FolderLoader.getMusicFolder(), "")){
            FolderLoader.readFolder2();
            if(!FolderLoader.musicFiles.isEmpty()){
                music = new Sound(FolderLoader.musicFiles.get(0));
                ObservableList<String> observableSongs = FXCollections.observableArrayList(FolderLoader.musicFiles);
                // Add data to the table
                stringRecordsTable.setItems(observableSongs);
                folderLabel.setText(FolderLoader.musicFolder);
                updateUIWithSound();
            }
        }

    }

    private void updateUIWithSound()  {
        // Update other UI elements as needed
        stringRecordsTable.refresh();
        songLabel.setText(FolderLoader.musicFiles.get(songPlaying));
        // Update cover art
        Artwork coverArt = music.getCoverArt();
        if (coverArt != null) {
            try {
                Image image = SwingFXUtils.toFXImage(coverArt.getImage(), null);
                imageView.setImage(image);
            }
            catch (IOException i){
                System.out.println("getimage in controller failed");
            }
        }
        if(Sound.paused){
            playIcon.setImage(new Image("pause.png"));
        }
        else {
            playIcon.setImage(new Image("play.png"));
            music.changeVolume(volumeSlider.getValue());
        }
    }


    public void browseMusic(ActionEvent event) {
        boolean change = FolderLoader.readFolder2(event);
        if(change){
            songPlaying = 0;
            ObservableList<String> observableSongs = FXCollections.observableArrayList(FolderLoader.musicFiles);
            // Add data to the table
            stringRecordsTable.setItems(observableSongs);
            folderLabel.setText(FolderLoader.musicFolder);

            if(music != null){
                music.changeFolder(FolderLoader.musicFiles.get(songPlaying));
            }
            else {
                music = new Sound(FolderLoader.musicFiles.get(songPlaying));
            }

            updateUIWithSound();
        }
    }



    public void play() {
        if(FolderLoader.musicFiles.get(songPlaying) != null){
            if (Sound.paused) {
                music.play();
                pauseButton.setGraphic(playIcon);
            }
            else {
                music.pause();
                pauseButton.setGraphic(playIcon);
            }
            updateUIWithSound();
        }
    }

    public void previousMedia() {
        if (songPlaying > 0) {
            songPlaying--;
            music.change(FolderLoader.musicFiles.get(songPlaying));
        }
        else {
            songPlaying = FolderLoader.musicFiles.size() - 1;
            music.change(FolderLoader.musicFiles.get(FolderLoader.musicFiles.size() - 1));
        }
        music.play();
        updateUIWithSound();
    }

    public void nextMedia() {
        if (songPlaying < FolderLoader.musicFiles.size() - 1) {
            songPlaying++;
            music.change(FolderLoader.musicFiles.get(songPlaying));
        }
        else {
            songPlaying = 0;
            music.change(FolderLoader.musicFiles.get(0));
        }
        updateUIWithSound();
    }
    private void updateCurrentlyPlaying(int clickedIndex) {
        songPlaying = clickedIndex;
        music.change(FolderLoader.musicFiles.get(songPlaying));
        pauseButton.setGraphic(playIcon);
        updateUIWithSound();
    }

    public void changeSpeed(ActionEvent event) {
        /*if (speedBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        }*/
    }



    public void closeApp() {
        Platform.exit();
        System.exit(0);
    }
    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                double current = music == null ? 0 : music.getCurrent();
                double end = music == null ? 0 : music.getEnd();
                songProgressBar.setProgress(current / end);
            }
        };
        timer.scheduleAtFixedRate(task, 0, 500);
    }

    public void cancelTimer() {
        //running = false;
    }



}