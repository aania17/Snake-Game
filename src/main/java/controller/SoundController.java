package controller;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * SoundController — loads and plays audio clips.
 *
 * FIXED:
 * 1. Resource path changed to /assets/sound/ (files must live in
 *    src/main/resources/assets/sound/ for Maven to put them on classpath).
 * 2. Null guard in setFile() — prints a clear error if the .wav
 *    can't be found instead of throwing a cryptic NullPointerException.
 * 3. Previous clip is stopped and closed before opening a new one
 *    to prevent resource leaks and audio glitches.
 */
public class SoundController {

    private Clip clip;
    private final URL[] soundURL = new URL[30];

    public SoundController() {
        soundURL[0] = getClass().getResource("/assets/sound/eated.wav");
        soundURL[1] = getClass().getResource("/assets/sound/gameover.wav");
        soundURL[2] = getClass().getResource("/assets/sound/BlueBoyAdventure.wav");
        soundURL[3] = getClass().getResource("/assets/sound/hitmonster.wav");
        soundURL[4] = getClass().getResource("/assets/sound/levelup.wav");
        soundURL[5] = getClass().getResource("/assets/sound/powerup.wav");

        // Log missing files immediately at startup so you know right away
        String[] names = {"eated","gameover","BlueBoyAdventure","hitmonster","levelup","powerup"};
        for (int i = 0; i < names.length; i++) {
            if (soundURL[i] == null) {
                System.err.println("[SoundController] WARNING: /assets/sound/"
                        + names[i] + ".wav not found on classpath. "
                        + "Move it to src/main/resources/assets/sound/");
            }
        }
    }

    public void setFile(int i) {
        // ── Guard: missing resource ───────────────────────────────────
        if (soundURL[i] == null) {
            System.err.println("[SoundController] Cannot play sound index " + i
                    + " — URL is null (file not on classpath).");
            return;
        }

        // ── Close previous clip to prevent resource leak ──────────────
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
        }

        // ── Load new clip ─────────────────────────────────────────────
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            System.err.println("[SoundController] Failed to load sound index " + i);
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null && clip.isOpen()) {
            clip.setFramePosition(0);   // rewind so replaying works correctly
            clip.start();
        }
    }

    public void loop() {
        if (clip != null && clip.isOpen()) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}