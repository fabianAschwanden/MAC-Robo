package org.example.robo.core.engine;

import org.example.robo.core.profile.RobustSelector;
import org.example.robo.core.profile.WebEventType;
import org.example.robo.core.profile.WebRecordingStep;

import java.util.List;

/**
 * Aufzeichnet Browser-Interaktionen als Sequenz von {@link WebRecordingStep}-Objekten.
 *
 * <p>Im Gegensatz zum koordinatenbasierten {@link MacroRecorder} speichert dieser
 * Recorder DOM-Selektoren (CSS/XPath), um robuste, layout-unabhängige Schritte zu erzeugen.
 *
 * <h3>Idle-Threshold (REQ-W005–W007)</h3>
 * Benutzerpausen über {@code idleThresholdMs} werden automatisch auf den Standardwert
 * {@link org.example.robo.util.Constants#DEFAULT_STEP_DELAY_MS} gekürzt und als
 * {@link org.example.robo.core.profile.DelayType#SMART} markiert.
 */
public interface WebMacroRecorder {

    /**
     * Startet eine neue Aufzeichnungssitzung.
     *
     * @param macroId eindeutige ID des Makros
     * @param name    benutzerfreundlicher Name
     */
    void startRecording(String macroId, String name);

    /** Beendet die laufende Aufzeichnung. */
    void stopRecording();

    /** Gibt zurück, ob gerade eine Aufzeichnung läuft. */
    boolean isRecording();

    /**
     * Zeichnet einen einzelnen Interaktionsschritt auf.
     *
     * @param eventType Art der Interaktion (CLICK, TYPE, HOVER, …)
     * @param selector  DOM-Selektor des Ziel-Elements
     * @param payload   optionale Nutzdaten (z.B. Eingabe-Text bei TYPE, URL bei NAVIGATE)
     */
    void recordStep(WebEventType eventType, RobustSelector selector, String payload);

    /** Gibt eine unveränderliche Sicht auf alle aufgezeichneten Schritte zurück. */
    List<WebRecordingStep> getRecordedSteps();

    /** Löscht alle aufgezeichneten Schritte ohne die Sitzung neu zu starten. */
    void clearSteps();

    /**
     * Setzt den Idle-Threshold in Millisekunden.
     * Pausen über diesem Wert werden auf den Standardwert gekürzt (REQ-W005).
     *
     * @param thresholdMs Schwellwert in ms (Standard: 2000)
     */
    void setIdleThresholdMs(long thresholdMs);
}
