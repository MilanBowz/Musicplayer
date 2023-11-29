package bowzgore.milan.musicfolderplayer.rest;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import javax.swing.*;
import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.stream.Stream;


public class FolderLoader {

    public static String musicFolder = "";

    // Define a key for the music folder preference
    public static final String MUSIC_FOLDER = "musicFolder";
    static List<String> musicFiles = new ArrayList<>();
    static List<String> folders = new ArrayList<>();
    static int folderIndex = -1;

    public static void readFolder2() {
        musicFolder = getMusicFolder();
        if(!Objects.equals(musicFolder, "")){
            retrieveMP3Files(new File(musicFolder));
        }
    }
    public static boolean readFolder2(ActionEvent event) {
        Window window = ((Node) event.getSource()).getScene().getWindow();
        DirectoryChooser dc = new DirectoryChooser();

        if (!musicFolder.isEmpty()) {
            File initialDirectory = new File(musicFolder);
            dc.setInitialDirectory(initialDirectory);
        }
        File selectedFolder = dc.showDialog(window);

        if (selectedFolder != null && !(selectedFolder.getAbsolutePath() +  File.separator).equals(musicFolder)) {
            musicFiles.clear(); // Clear previous selections
            musicFolder = selectedFolder.getAbsolutePath() + File.separator;
            getPreferences().put(MUSIC_FOLDER, musicFolder);
            System.out.println(musicFolder);

            // Retrieve all MP3 files within the selected folder
            retrieveMP3Files(selectedFolder);
            //retrieveFirstFolder(selectedFolder);
            return true;
        }
        else {
            // User canceled the folder selection
            return false;
        }
    }
    private static void retrieveMP3Files(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (files != null && files.length>0) {
                System.out.println(files.length);
                for (File file : files) {
                    musicFiles.add(file.getName());
                }
            }
            else{
                retrieveFolders(folder);
            }
        }
    }

    private static void retrieveFolders(File folder) {
        folders.clear();
        folderIndex = -1;
        File[] subdirectories = folder.listFiles(File::isDirectory);
        if (subdirectories != null && subdirectories.length > 0) {
            System.out.println(subdirectories.length);
            for (File subdirectory : subdirectories) {
                if(!subdirectory.isHidden()){
                    folders.add(subdirectory.getName());
                }
            }
            nextFolder();
        }
    }
    public static void nextFolder() {
        if(folders.size()-1>folderIndex){
            folderIndex+=1;
        }
        else {
            folderIndex=0;
        }
        if(!readSubFolder()){
            folders.remove(folderIndex);
            readSubFolder();
        }
    }
    public static void previousFolder() {
        if(0 == folderIndex){
            folderIndex =folders.size()-1;
        }
        else {
            folderIndex-=1;
        }
        if(!readSubFolder()){
            folders.remove(folderIndex);
            readSubFolder();
        }
    }
    public static boolean readSubFolder(){
        File initialDirectory = new File(musicFolder+folders.get(folderIndex));
        File[] filesSub = initialDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        musicFiles.clear();
        if (filesSub != null && filesSub.length > 0) {
            for (File file : filesSub) {
                musicFiles.add(folders.get(folderIndex)+ File.separator+file.getName());
                System.out.println(folders.get(folderIndex)+ File.separator+file.getName());
            }
            System.out.println(Arrays.toString(filesSub));
            return true;
        }
        return false;
    }

    // Method to list subdirectories of a given directory
    private static Preferences getPreferences() {
        // Retrieve the user preferences node for your application
        return Preferences.userNodeForPackage(FolderLoader.class);
    }
    public static String getMusicFolder() {
        // Retrieve the music folder from preferences, default to an empty string if not found
        return getPreferences().get(MUSIC_FOLDER, "");
    }
    public static void resetMusicFolder() {
        // Reset the stored music folder value in preferences
        getPreferences().put(MUSIC_FOLDER,"");
    }

}
