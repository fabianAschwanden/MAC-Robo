package org.example.robo.core.engine;

import org.example.robo.core.profile.Macro;

/**
 * Interface für Macro Playback.
 */
public interface MacroPlayer {
    void play(Macro macro);
    void stop();
    boolean isPlaying();
}

