package org.example.robo.core.engine;

import org.example.robo.core.profile.Macro;

/**
 * Interface für Macro Recording.
 */
public interface MacroRecorder {
    void startRecording(String macroId, String name);
    void stopRecording();
    boolean isRecording();
    Macro getCurrentMacro();
}

