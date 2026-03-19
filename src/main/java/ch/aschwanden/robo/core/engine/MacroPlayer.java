package ch.aschwanden.robo.core.engine;

import ch.aschwanden.robo.core.profile.Macro;

/**
 * Interface für Macro Playback.
 */
public interface MacroPlayer {
    void play(Macro macro);
    void stop();
    boolean isPlaying();
}

