package org.example.robo.core.engine;

import org.example.robo.core.profile.ClickType;
import org.example.robo.core.profile.Macro;
import org.example.robo.core.profile.MacroEvent;
import org.example.robo.core.profile.MouseClickEvent;
import org.example.robo.core.profile.MouseMoveEvent;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein einfacher Recorder, der MouseMove und MouseClick Events sammelt.
 * Diese MVP-Variante nutzt aktive Abfrage der Maus-Position und externe Hooks
 * für Klick-Events (Start/Stop) - in einer realen Implementierung würde ein globaler
 * Hook die Events pushen.
 */
public class MacroRecorderImpl implements MacroRecorder {
    private static final Logger logger = LoggerFactory.getLogger(MacroRecorderImpl.class);

    private volatile boolean recording = false;
    private Macro currentMacro;
    private long recordingStartMs;
    private final ClickEngine clickEngine;

    public MacroRecorderImpl(ClickEngine clickEngine) {
        this.clickEngine = clickEngine;
    }

    @Override
    public synchronized void startRecording(String macroId, String name) {
        if (recording) {
            logger.warn("Already recording");
            return;
        }
        this.currentMacro = new Macro(macroId, name);
        this.currentMacro.setEvents(new ArrayList<>());
        this.recordingStartMs = System.currentTimeMillis();
        this.recording = true;
        logger.info("Started recording macro {} ({})", name, macroId);
    }

    @Override
    public synchronized void stopRecording() {
        if (!recording) {
            logger.warn("Not recording");
            return;
        }
        this.recording = false;
        logger.info("Stopped recording macro {} ({} events)", currentMacro.getName(), currentMacro.getEvents().size());
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public Macro getCurrentMacro() {
        return currentMacro;
    }

    // Hilfsmethoden - werden von UI/Keyboard Hooks aufgerufen
    public synchronized void recordMouseMove(MousePosition position) {
        if (!recording) return;
        long ts = System.currentTimeMillis() - recordingStartMs;
        MouseMoveEvent ev = new MouseMoveEvent(ts, position);
        currentMacro.getEvents().add(ev);
    }

    public synchronized void recordMouseClick(MousePosition position, ClickType clickType) {
        if (!recording) return;
        long ts = System.currentTimeMillis() - recordingStartMs;
        MouseClickEvent ev = new MouseClickEvent(ts, position, clickType);
        currentMacro.getEvents().add(ev);
    }
}

