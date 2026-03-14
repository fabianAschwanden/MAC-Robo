package org.example.robo.core.engine;

import org.example.robo.core.profile.ClickProfile;
import org.example.robo.util.MousePosition;

/**
 * Interface für den Click Engine - Core der Anwendung.
 * Verantwortlich für die Durchführung von automatisierten Mausklicks.
 */
public interface ClickEngine {

    /**
     * Startet die automatisierte Klick-Simulation mit dem gegebenen Profil.
     *
     * @param profile Das zu verwendende Klick-Profil
     * @throws IllegalStateException wenn bereits Klicks aktiv sind
     */
    void startClicking(ClickProfile profile);

    /**
     * Stoppt die automatisierte Klick-Simulation.
     */
    void stopClicking();

    /**
     * Prüft, ob die Klick-Simulation aktuell läuft.
     *
     * @return true wenn aktiv, false sonst
     */
    boolean isRunning();

    /**
     * Setzt die Klick-Frequenz in Hz (Klicks pro Sekunde).
     *
     * @param hz Frequenz (1-100)
     */
    void setClickFrequency(int hz);

    /**
     * Setzt die Mausposition für Klicks.
     *
     * @param position neue Position
     */
    void setClickPosition(MousePosition position);

    /**
     * Gibt die aktuelle Mausposition zurück.
     *
     * @return aktuelle Mausposition
     */
    MousePosition getCurrentMousePosition();

    /**
     * Registriert einen Listener für Engine-Events.
     *
     * @param listener der zu registrierende Listener
     */
    void addClickEngineListener(ClickEngineListener listener);

    /**
     * Entfernt einen registrierten Listener.
     *
     * @param listener der zu entfernende Listener
     */
    void removeClickEngineListener(ClickEngineListener listener);

    /**
     * Gibt das aktuelle aktive Profil zurück.
     *
     * @return das aktive Profil oder null
     */
    ClickProfile getCurrentProfile();
}

