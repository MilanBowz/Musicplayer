package bowzgore.milan.musicfolderplayer.rest;


// https://www.youtube.com/watch?v=Ope4icw6bVk


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;


import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;

import org.jaudiotagger.tag.TagException;
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

    public static Artwork coverArt;

    private Timer timer;
    private TimerTask task ;

    boolean changeOnce = false;


    // constructor to initialize streams and clip
    public Sound() {

    }
    // constructor to initialize streams and clip
    public Sound(String filelocation, Slider progressBar) {
        // create File object
        //initialize(null,null);
        beginTimer(progressBar);

        filePlaying = new File(FolderLoader.musicFolder + filelocation).getAbsoluteFile();
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        loadMetadata();
    }
    private void loadMetadata()  {
        disableJaudiotaggerLogs();
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
    }
    private static void disableJaudiotaggerLogs() {
        // Get the logger for Jaudiotagger
        Logger jaudiotaggerLogger = Logger.getLogger("org.jaudiotagger");

        // Set the logging level to WARNING
        jaudiotaggerLogger.setLevel(Level.WARNING);

        // Disable console handler
        for (Handler handler : jaudiotaggerLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                jaudiotaggerLogger.removeHandler(handler);
            }
        }
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
        //start the media
        mediaPlayer.stop();
        //start the media
        mediaPlayer.play();
        paused = false;
    }
    public void pause() {
        //start the media
        mediaPlayer.pause();
        paused = true;
    }
    public void changeSong(){
        //start the media
        mediaPlayer.stop();
    }
    public void changeVolume(double volume) {
        //start the media
        mediaPlayer.setVolume(volume*0.01);
    }
    public void changeSpeed(double speed) {
        mediaPlayer.setRate(speed);
    }

    public void change(String file) {
        mediaPlayer.stop();
        filePlaying = new File(FolderLoader.musicFolder + file).getAbsoluteFile();
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        replay();
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



}
