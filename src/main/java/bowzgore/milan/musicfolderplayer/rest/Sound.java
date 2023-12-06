package bowzgore.milan.musicfolderplayer.rest;


// https://www.youtube.com/watch?v=Ope4icw6bVk


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;


import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import org.jaudiotagger.tag.datatype.Artwork;

public class Sound {
    private File filePlaying;
    private MediaPlayer mediaPlayer;

    // to store current position
    //Long currentFrame;
    // current status of clip
    //String status = String.valueOf(mediaPlayer.statusProperty());
    private Media media;

    public static boolean paused = true;

    private static Artwork coverArt;

    private Timer timer;
    private TimerTask task ;

    private boolean changeOnce = false;

    private CompletableFuture<Void> metadataFuture;
    private static ExecutorService metadataExecutor = Executors.newSingleThreadExecutor();

    // constructor to initialize streams and clip
    public Sound(String filelocation, Slider progressBar) {
        // create File object
        //initialize(null,null);
        beginTimer(progressBar);
        filePlaying = new File(FolderLoader.musicFolder + filelocation).getAbsoluteFile();
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);//play song in loop
        barInit(progressBar);
        loadMetadata();
    }
    private void loadMetadata()  {
        // Create a CompletableFuture for metadata loading
        metadataFuture = CompletableFuture.runAsync(() -> {
            try {
                AudioFile audioFile = AudioFileIO.read(filePlaying);
                Tag tag = audioFile.getTag();
                // Get the cover art
                if (tag != null) {
                    coverArt = tag.getFirstArtwork();
                }
            } catch (Exception e) {
                // Handle the exception without printing the stack trace
                // You can log a message or perform other actions as needed
                System.out.println("Error loading metadata: " + filePlaying);
            }
        }, metadataExecutor);
    }

    // Getter method for cover art
    public Artwork getCoverArt() {
        return coverArt;
    }


    // Method to play the audio
    public void play() {
        //start the media
        mediaPlayer.play();
        paused = false;
    }
    public void replay() {
        mediaPlayer.seek(Duration.millis(0));
    }
    public void restart() {
        mediaPlayer.stop();
        mediaPlayer.play();
    }
    public void pause() {
        //start the media
        mediaPlayer.pause();
        paused = true;
    }
    public void changeVolume(double volume) {
        //start the media
        mediaPlayer.setVolume(volume*0.01);
    }

    public void change(String file) {
        mediaPlayer.stop();
        filePlaying = new File(FolderLoader.musicFolder + file).getAbsoluteFile();
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        restart();
        loadMetadata();
        paused = false;
    }
    public void changeFolder(String file) {
        mediaPlayer.stop();
        filePlaying = new File(FolderLoader.musicFolder + file).getAbsoluteFile();
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        loadMetadata();
        paused = true;
    }
    public void stopMusic(){
        timer.cancel();
        timer.purge();
        mediaPlayer.stop();
        paused = true;
    }

    public double getEnd() {
        return media == null ? 0 : media.getDuration().toSeconds();
    }

    public double getCurrent() {
        return mediaPlayer == null ? 0 : mediaPlayer.getCurrentTime().toSeconds();
    }
    public void behaviourOnEndMedia(Runnable onEndCallback){
        mediaPlayer.setOnEndOfMedia(onEndCallback);
    }

    public void barInit(Slider progressBar){
        progressBar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(progressBar.isPressed()){
                    changeOnce = true;
                }
                if(!changeOnce) {
                    String style = String.format("-fx-background-color: linear-gradient(to right, #2D819D %d%%, #969696 %d%%);",
                            newValue.intValue()+1, newValue.intValue()+1);
                    progressBar.lookup(".track").setStyle(style);
                }
            }
        });
    }
    public void beginTimer(Slider progressBar) {
        timer = new Timer();
        task = new TimerTask() {
            double value = 0;
            double percentage = 0;
            public void run() {
                if(!progressBar.isPressed()){
                    if (changeOnce) {
                        percentage = (progressBar.getValue()/progressBar.getMax());
                        value =  percentage * getEnd();
                        setCurrent(value);
                        changeOnce = false;
                    }
                    else {
                        value = (getCurrent() / getEnd()) *100;
                        //System.out.println(value);
                        // This change was not initiated by the user (programmatic change)
                        progressBar.setValue(value);
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 250);
    }
    // Method to pause the timer
    public void setCurrent(double seconds) {
        mediaPlayer.seek(Duration.seconds(seconds));
    }

    public CompletableFuture<Void> getMetadataFuture() {
        return metadataFuture;
    }
}
