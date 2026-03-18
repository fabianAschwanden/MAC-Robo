package org.example.robo.core.engine;

import org.example.robo.core.profile.DelayType;
import org.example.robo.core.profile.RobustSelector;
import org.example.robo.core.profile.WebEventType;
import org.example.robo.core.profile.WebRecordingStep;
import org.example.robo.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-sichere Implementierung des {@link WebMacroRecorder}.
 *
 * <h3>Idle-Threshold-Logik</h3>
 * <ul>
 *   <li>Pausen ≤ {@code idleThresholdMs} → unveränderter Wert, {@link DelayType#HARD}</li>
 *   <li>Pausen > {@code idleThresholdMs} → auf {@link Constants#DEFAULT_STEP_DELAY_MS} gekürzt,
 *       {@link DelayType#SMART} (Warten auf Element statt fester Zeit)</li>
 * </ul>
 */
public class WebMacroRecorderImpl implements WebMacroRecorder {

    private static final Logger logger = LoggerFactory.getLogger(WebMacroRecorderImpl.class);

    private volatile boolean recording = false;
    private volatile long idleThresholdMs = Constants.IDLE_THRESHOLD_MS;
    private volatile long lastEventTimeMs;
    private volatile long recordingStartMs;

    private String currentMacroId;
    private String currentMacroName;
    private final List<WebRecordingStep> steps = new ArrayList<>();

    @Override
    public synchronized void startRecording(String macroId, String name) {
        this.currentMacroId = macroId;
        this.currentMacroName = name;
        this.steps.clear();
        this.recordingStartMs = System.currentTimeMillis();
        this.lastEventTimeMs = recordingStartMs;
        this.recording = true;
        logger.info("Web-Aufzeichnung gestartet: {} ({})", name, macroId);
    }

    @Override
    public synchronized void stopRecording() {
        this.recording = false;
        logger.info("Web-Aufzeichnung beendet. {} Schritte aufgezeichnet.", steps.size());
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public synchronized void recordStep(WebEventType eventType, RobustSelector selector, String payload) {
        if (!recording) {
            logger.warn("recordStep aufgerufen, obwohl keine Aufzeichnung läuft – Schritt wird ignoriert.");
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - recordingStartMs;
        long rawDelay = now - lastEventTimeMs;

        // Idle-Threshold: Pausen über dem Schwellwert werden gekürzt (REQ-W005)
        long effectiveDelay;
        DelayType delayType;
        if (rawDelay > idleThresholdMs) {
            effectiveDelay = Constants.DEFAULT_STEP_DELAY_MS;
            delayType = DelayType.SMART; // Warten auf Element statt fixer Zeit (REQ-W006)
            logger.debug("Idle-Pause von {}ms auf {}ms gekürzt → DelayType.SMART", rawDelay, effectiveDelay);
        } else {
            effectiveDelay = rawDelay;
            delayType = DelayType.HARD;
        }

        WebRecordingStep step = WebRecordingStep.builder()
                .timestamp(elapsed)
                .webEventType(eventType)
                .selector(selector)
                .payload(payload)
                .timing(effectiveDelay)
                .delayType(delayType)
                .build();

        steps.add(step);
        lastEventTimeMs = now;

        logger.debug("Schritt aufgezeichnet: {}", step);
    }

    @Override
    public List<WebRecordingStep> getRecordedSteps() {
        return Collections.unmodifiableList(steps);
    }

    @Override
    public void setIdleThresholdMs(long thresholdMs) {
        this.idleThresholdMs = thresholdMs;
        logger.debug("Idle-Threshold auf {}ms gesetzt.", thresholdMs);
    }

    @Override
    public synchronized void clearSteps() {
        steps.clear();
        lastEventTimeMs = System.currentTimeMillis();
        logger.debug("Aufgezeichnete Schritte gelöscht.");
    }

    public String getCurrentMacroId() {
        return currentMacroId;
    }

    public String getCurrentMacroName() {
        return currentMacroName;
    }
}
