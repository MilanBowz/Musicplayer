package bowzgore.milan.musicfolderplayer.rest;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.converter.StringConverter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.*;
import java.util.Timer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import org.jaudiotagger.tag.datatype.Artwork;


public class UI implements Initializable {

    @FXML
    public Label folderLabel;
    @FXML
    public Button loopButton;
    @FXML
    public ListView<String> stringRecordsList;
    @FXML
    private Label songLabel;
    @FXML
    private Button pauseButton, resetButton, previousButton, nextButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider songProgressBar;
    @FXML
    private ImageView imageView;

    public Sound music;

    private int songPlaying = 0;
    private boolean loopList = false;


    ImageView playIcon = new ImageView(new Image("play.png"));
    ImageView loopIcon = new ImageView(new Image("loop_list.png"));


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(!Objects.equals(FolderLoader.getMusicFolder(), "")){
            FolderLoader.readFolderInit();
            this.initFolder();
        }

        playIcon.setFitHeight(40);
        playIcon.setFitWidth(40);
        playIcon.setPreserveRatio(true);

        loopIcon.setFitHeight(40);
        loopIcon.setFitWidth(40);
        loopIcon.setPreserveRatio(true);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                music.changeVolume(volumeSlider.getValue());
            }

        });


        stringRecordsList.setCellFactory(tv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                if (Objects.equals(item, FolderLoader.musicFiles.get(songPlaying))) {
                    setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
                }
                else {
                    setStyle("-fx-background-color: black; -fx-text-fill: white;");
                    super.updateItem(item, empty);
                }
                setText(item);
            }
            {
                // Add an event handler to the row
                setOnMouseClicked(event -> {
                    if (!isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                        // Update the currently playing song based on the clicked row
                        int clickedIndex = getIndex();
                        updateCurrentlyPlaying(clickedIndex);
                    }
                });
            }

            // Add your logic for handling the selected item
        });
    }

    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        // Get the key code of the pressed key
        KeyCode keyCode = event.getCode();
        // Check for Space or Enter key
        if (keyCode == KeyCode.SPACE || keyCode == KeyCode.ENTER) {
            play();
        }
        else if (keyCode == KeyCode.DIGIT2 | keyCode == KeyCode.NUMPAD2) {
            nextMedia();
        }
        else if (keyCode == KeyCode.DIGIT1 | keyCode == KeyCode.NUMPAD1) {
            previousMedia();
        }
        else if (keyCode == KeyCode.ADD) {
            volumeSlider.setValue(volumeSlider.getValue()+5);
        }
        else if (keyCode == KeyCode.SUBTRACT) {
            volumeSlider.setValue(volumeSlider.getValue()-5);
        }
    }


    private void updateUIWithSound()  {
        // Update other UI elements as needed
        stringRecordsList.refresh();
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
        getLoop();
    }


    public void browseMusic(ActionEvent event) {
        if(FolderLoader.readFolderBrowse(event)){
            if(!FolderLoader.musicFiles.isEmpty()){
                songPlaying = 0;
                folderLabel.setText(FolderLoader.musicFolder);
                if(music != null){
                    music.changeFolder(FolderLoader.musicFiles.get(songPlaying));
                }
                else {
                    music = new Sound(FolderLoader.musicFiles.get(songPlaying),songProgressBar);
                }
                playIcon.setImage(new Image("play.png"));
                pauseButton.setGraphic(playIcon);
                updateUIWithSound();
            }
        }
    }



    public void play() {
        if(FolderLoader.musicFiles.get(songPlaying) != null){
            if (Sound.paused) {
                music.play();
                playIcon.setImage(new Image("pause.png"));
                pauseButton.setGraphic(playIcon);
            }
            else {
                music.pause();
                playIcon.setImage(new Image("play.png"));
                music.changeVolume(volumeSlider.getValue());
                pauseButton.setGraphic(playIcon);
            }
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
        playIcon.setImage(new Image("play.png"));
        updateUIWithSound();
        music.play();
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
        playIcon.setImage(new Image("play.png"));
        updateUIWithSound();
        music.play();
    }
    private void updateCurrentlyPlaying(int clickedIndex) {
        songPlaying = clickedIndex;
        music.change(FolderLoader.musicFiles.get(songPlaying));
        pauseButton.setGraphic(playIcon);
        playIcon.setImage(new Image("pause.png"));
        updateUIWithSound();
    }

    public void StopApp() {
        // Stop the sound
        if (music != null) {
            music.stopMusic();
        }
    }
    public void initFolder(){
        if(!FolderLoader.musicFiles.isEmpty()){
            songPlaying = 0;
            if(music != null){
                music.changeFolder(FolderLoader.musicFiles.get(songPlaying));
            }
            else {
                music = new Sound(FolderLoader.musicFiles.get(songPlaying),songProgressBar);
            }
            stringRecordsList.setItems(FolderLoader.musicFiles);
            folderLabel.setText(FolderLoader.musicFolder);
            playIcon.setImage(new Image("play.png"));
            updateUIWithSound();
        }
    }

    public void previousList() {
        if(FolderLoader.folders.size() > 1){
            FolderLoader.previousFolder();
            initFolder();
        }
    }

    public void nextList() {
        if(FolderLoader.folders.size() > 1){
            FolderLoader.nextFolder();
            initFolder();
        }
    }

    public void changeLoop() {
        if(loopList){
            loopList = false;
            music.behaviourOnEndMedia(music::replay);
            loopIcon.setImage(new Image("loop.png"));
            loopButton.setGraphic(loopIcon);
        }
        else {
            loopList = true;
            music.behaviourOnEndMedia(this::nextMedia);
            loopIcon.setImage(new Image("loop_list.png"));
            loopButton.setGraphic(loopIcon);
        }
    }
    public void getLoop() {
        if(!loopList){
            music.behaviourOnEndMedia(music::replay);
        }
        else {
            music.behaviourOnEndMedia(this::nextMedia);
        }
    }
}