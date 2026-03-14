package org.example.robo.core.engine;

import org.example.robo.util.MousePosition;

/**
 * Listener Interface für Click Engine Events.
 * Ermöglicht anderen Komponenten (z.B. UI) auf Änderungen zu reagieren.
 */
public interface ClickEngineListener {

    /**
     * Wird aufgerufen, wenn ein Klick ausgeführt wurde.
     *
     * @param position Position des Klicks
     */
    void onClickExecuted(MousePosition position);

    /**
     * Wird aufgerufen, wenn die Engine gestartet wurde.
     */
    void onEngineStarted();

    /**
     * Wird aufgerufen, wenn die Engine gestoppt wurde.
     */
    void onEngineStopped();

    /**
     * Wird aufgerufen, wenn ein Fehler auftritt.
     *
     * @param errorMessage Fehlermeldung
     */
    void onError(String errorMessage);
}

