package ch.aschwanden.robo.core.engine;

import ch.aschwanden.robo.core.profile.Macro;

/**
 * Interface für Macro Recording.
 */
public interface MacroRecorder {
    void startRecording(String macroId, String name);
    void stopRecording();
    boolean isRecording();
    Macro getCurrentMacro();
}

