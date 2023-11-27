package bowzgore.milan.musicfolderplayer.rest;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
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
    public Region progressOverlay;
    @FXML
    private Pane pane;
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

    @FXML
    private TableView<String> stringRecordsTable;

    public Sound music;

    public int songPlaying = 0;

    ImageView playIcon = new ImageView(new Image("pause.png"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up the table columns
        TableColumn<String, String> recordsColumn = new TableColumn<>();
        recordsColumn.setPrefWidth(400);

        // Set up the table columns
        recordsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        recordsColumn.setSortType(TableColumn.SortType.ASCENDING);
        recordsColumn.sortableProperty().setValue(false);
        // Add columns to the table
        stringRecordsTable.getColumns().add(recordsColumn);
        stringRecordsTable.setTableMenuButtonVisible(false);
        stringRecordsTable.setContextMenu(null);

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

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                music.changeVolume(volumeSlider.getValue());
            }

        });

        // Set up the table
        stringRecordsTable.setRowFactory(tv -> new TableRow<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                // Set the background color for a specific ro
                    super.updateItem(item,empty);
                    if(getIndex() == songPlaying){
                        setStyle("-fx-background-color: darkblue;-fx-text-fill: white;");
                    }
                    else {
                        setStyle("-fx-text-fill: white;");
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
                ObservableList<String> observableSongs = FXCollections.observableArrayList(FolderLoader.musicFiles);
                // Add data to the table
                stringRecordsTable.setItems(observableSongs);
                folderLabel.setText(FolderLoader.musicFolder);
                music = new Sound(FolderLoader.musicFiles.get(0),songProgressBar);
                updateUIWithSound();
            }
        }
        songProgressBar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(songProgressBar.isPressed()){
                    music.changeOnce = true;
                }
                if(!music.changeOnce) {
                    String style = String.format("-fx-background-color: linear-gradient(to right, #2D819D %d%%, #969696 %d%%);",
                            newValue.intValue()+1, newValue.intValue()+1);
                    songProgressBar.lookup(".track").setStyle(style);
                }
            }
        });

    }

    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        // Get the key code of the pressed key
        KeyCode keyCode = event.getCode();

        // Check for Space or Enter key
        if (keyCode == KeyCode.SPACE || keyCode == KeyCode.ENTER) {
            play();
        }
        else if (keyCode == KeyCode.N) {
            nextMedia();
        }
        else if (keyCode == KeyCode.P) {
            previousMedia();
        }
        else if (keyCode == KeyCode.DOWN) {
            music.changeVolume(volumeSlider.getValue()-5);
        }
        else if (keyCode == KeyCode.UP) {
            music.changeVolume(volumeSlider.getValue()+5);
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
            if(!FolderLoader.musicFiles.isEmpty()){
                songPlaying = 0;
                ObservableList<String> observableSongs = FXCollections.observableArrayList(FolderLoader.musicFiles);
                // Add data to the table
                stringRecordsTable.setItems(observableSongs);
                folderLabel.setText(FolderLoader.musicFolder);
                if(music != null){
                    music.changeFolder(FolderLoader.musicFiles.get(songPlaying));
                }
                else {
                    music = new Sound(FolderLoader.musicFiles.get(songPlaying),songProgressBar);
                }
                updateUIWithSound();
            }
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

    public void StopApp() {
        // Stop the sound
        if (music != null) {
            music.stopMusic();
        }
    }



}