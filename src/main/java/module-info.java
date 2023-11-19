module bowzgore.milan.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.media;
    requires java.desktop;
    requires javafx.swing;
    requires jaudiotagger;
    requires java.prefs;
    requires com.sun.jna;
    requires org.controlsfx.controls;
    requires java.logging;


    exports bowzgore.milan.musicfolderplayer.rest;
    opens bowzgore.milan.musicfolderplayer.rest to javafx.fxml;
    exports bowzgore.milan.musicfolderplayer;
    opens bowzgore.milan.musicfolderplayer to javafx.fxml;
}