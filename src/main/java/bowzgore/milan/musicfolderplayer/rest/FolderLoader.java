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


public class FolderLoader {

    public static String musicFolder = "";
    // Define a key for the music folder preference
    public static final String MUSIC_FOLDER = "musicFolder";
    static List<String> musicFiles = new ArrayList<>();
    public static List<String> readFolder() {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();

        // Set the file chooser to open only directories
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Show the dialog to select a directory
        int result = fileChooser.showOpenDialog(null);

        // Check if the user selected a directory
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected directory
            File selectedDirectory = fileChooser.getSelectedFile();

            // List the files in the selected directory
            if (selectedDirectory.isDirectory()) {
                System.out.println(selectedDirectory.getAbsolutePath());
                File[] files = selectedDirectory.listFiles();
                if (files != null) {
                    System.out.println("Files in the selected directory:");
                    for (File file : files) {
                        System.out.println(file.getName());
                        musicFiles.add(file.getName());
                    }
                }
                else {
                    System.out.println("No files in the selected directory.");
                }
            }
            else {
                System.out.println("Selected path is not a directory.");
            }
        } else {
            System.out.println("User canceled the operation.");
        }
        return musicFiles;
    }

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
            if (files != null) {
                for (File file : files) {
                    musicFiles.add(file.getName());
                }
            }
        }
    }
    private static void retrieveFirstFolder(File parentDirectory) {
        // List all files and directories in the provided directory
        File[] files = parentDirectory.listFiles();

        // Filter out only directories
        List<File> folderList = new ArrayList<>(Arrays.asList(files));

        // Find the first directory
        File firstFolder = folderList.stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElse(null);

        // Check if a directory was found
        if (firstFolder != null) {
            System.out.println("First folder found: " + firstFolder.getAbsolutePath());
        } else {
            System.out.println("No folders found in: " + parentDirectory.getAbsolutePath());
        }
    }
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
