package bowzgore.milan.musicfolderplayer.rest;


// https://www.youtube.com/watch?v=Ope4icw6bVk


import javafx.event.EventHandler;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;

import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

public class Sound extends Thread {
    private static File filePlaying;
    private static MediaPlayer mediaPlayer;


    // to store current position
    //Long currentFrame;
    // current status of clip
    //String status = String.valueOf(mediaPlayer.statusProperty());
    private static Media media;

    public static boolean paused = true;

    public static Artwork coverArt;

    MediaView mediaView;


    // constructor to initialize streams and clip
    public Sound(String filelocation) {
        // create File object
        //initialize(null,null);
        filePlaying = new File(FolderLoader.musicFolder + filelocation).getAbsoluteFile();
        System.out.println(filePlaying.toURI().toString());
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        loadMetadata();
        mediaView = new MediaView(mediaPlayer);
        mediaView.setOnError(new EventHandler<MediaErrorEvent>() {
            public void handle(MediaErrorEvent t) {
                // Handle asynchronous error in MediaView.
            }
        });
    }
    private void loadMetadata()  {

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
        //start the media
        mediaPlayer.stop();
        filePlaying = new File(FolderLoader.musicFolder + file).getAbsoluteFile();//
        media = new Media(filePlaying.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        replay();
        loadMetadata();
    }
    public void stopMusic(){
        mediaPlayer.stop();

        paused = true;
    }

    public double getEnd() {
        return media.getDuration().toSeconds();

    }




    public double getCurrent() {
        return mediaPlayer.getCurrentTime().toSeconds();
    }
    public void setCurrent(double seconds) {
        if (mediaPlayer != null) {
            Duration duration = Duration.seconds(seconds);
            mediaPlayer.seek(duration);
        }
    }


}
