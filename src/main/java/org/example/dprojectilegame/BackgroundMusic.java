package org.example.dprojectilegame;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Playlist UI: scan folder for {@code .mp3}/{@code .wav}, show current title,
 * transport {@code Previous → Stop → Resume → Next}. Starts stopped (no autoplay).
 * <ul>
 *   <li>Natural track end: advances to next file in order (wraps), unless the user had pressed Stop.</li>
 *   <li>Stop: silence until Resume; does not auto-advance.</li>
 *   <li>Previous/Next: change track; if something was playing, new track starts immediately.</li>
 * </ul>
 */
public final class BackgroundMusic {

    private static final String BTN_STYLE = "-fx-text-fill: white; -fx-background-color: #444444; "
            + "-fx-font-size: 11px;";

    private MediaPlayer player;
    private List<Path> tracks = new ArrayList<>();
    private int index = 0;
    private Label titleLabel;
    /** When true, {@link MediaPlayer} was stopped by the user — do not auto-advance on end. */
    private boolean userRequestedStop = true;

    public BackgroundMusic() {
    }

    /**
     * @param musicDir directory (created if missing)
     */
    public VBox buildSection(Path musicDir) throws IOException {
        Files.createDirectories(musicDir);
        tracks = listAudioFiles(musicDir);

        titleLabel = new Label();
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);
        updateTitleText();

        Button prev = new Button("Previous");
        Button stop = new Button("Stop");
        Button resume = new Button("Resume");
        Button next = new Button("Next");
        for (Button b : new Button[] { prev, stop, resume, next }) {
            b.setStyle(BTN_STYLE);
        }

        prev.setOnAction(e -> stepTrack(-1));
        next.setOnAction(e -> stepTrack(1));
        stop.setOnAction(e -> stopPlayback());
        resume.setOnAction(e -> resumePlayback());

        HBox row = new HBox(8, prev, stop, resume, next);

        Label header = new Label("Music");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        return new VBox(8, header, titleLabel, row);
    }

    private void updateTitleText() {
        if (tracks.isEmpty()) {
            titleLabel.setText("— (add .mp3 or .wav to folder)");
            return;
        }
        titleLabel.setText(tracks.get(index).getFileName().toString());
    }

    private void stepTrack(int delta) {
        if (tracks.isEmpty()) {
            return;
        }
        boolean playing = player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
        index = (index + delta + tracks.size()) % tracks.size();
        updateTitleText();
        if (playing) {
            userRequestedStop = false;
            playTrackAtCurrentIndex();
        } else {
            stopPlayback();
        }
    }

    /**
     * Stops playback; user intent is silence until Resume (no auto-advance on future events).
     */
    public void stopPlayback() {
        userRequestedStop = true;
        disposePlayerQuiet();
    }

    /**
     * Starts or restarts the current track; clears user-stop so natural end can advance the playlist.
     */
    private void resumePlayback() {
        if (tracks.isEmpty()) {
            return;
        }
        userRequestedStop = false;
        playTrackAtCurrentIndex();
    }

    private void playTrackAtCurrentIndex() {
        disposePlayerQuiet();
        Path path = tracks.get(index);
        try {
            Media media = new Media(path.toUri().toString());
            player = new MediaPlayer(media);
            player.setCycleCount(1);
            player.setOnEndOfMedia(this::onTrackEndedNaturally);
            player.play();
        } catch (RuntimeException ex) {
            System.err.println("Could not play: " + path + " — " + ex.getMessage());
        }
    }

    private void onTrackEndedNaturally() {
        if (userRequestedStop || tracks.isEmpty()) {
            return;
        }
        index = (index + 1) % tracks.size();
        updateTitleText();
        playTrackAtCurrentIndex();
    }

    private void disposePlayerQuiet() {
        if (player != null) {
            player.setOnEndOfMedia(null);
            player.stop();
            player.dispose();
            player = null;
        }
    }

    public void stop() {
        stopPlayback();
    }

    private static List<Path> listAudioFiles(Path dir) throws IOException {
        List<Path> out = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                if (n.endsWith(".mp3") || n.endsWith(".wav")) {
                    out.add(p);
                }
            }
        }
        out.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)));
        return out;
    }
}
