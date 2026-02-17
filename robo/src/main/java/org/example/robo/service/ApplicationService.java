package org.example.robo.service;

import org.example.robo.config.ConfigurationManager;
import org.example.robo.config.ConfigurationManagerImpl;
import org.example.robo.core.engine.ClickEngine;
import org.example.robo.core.engine.ClickEngineImpl;
import org.example.robo.core.input.HotkeyAction;
import org.example.robo.core.input.KeyboardListener;
import org.example.robo.core.input.KeyboardListenerImpl;
import org.example.robo.core.profile.ClickProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Zentrale Application Service für Koordination aller Komponenten.
 * Singleton-Pattern für globale Zugänglichkeit.
 */
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private static ApplicationService instance;

    private final ClickEngine clickEngine;
    private final KeyboardListener keyboardListener;
    private final ConfigurationManager configurationManager;

    /**
     * Erstellt eine neue ApplicationService Instanz.
     * Initialisiert alle Core-Komponenten.
     */
    private ApplicationService() {
        // Initialisiere Komponenten in korrekter Reihenfolge
        this.configurationManager = new ConfigurationManagerImpl();
        this.clickEngine = new ClickEngineImpl();
        this.keyboardListener = new KeyboardListenerImpl();

        // Starte Keyboard Listening
        this.keyboardListener.startListening();

        // Registriere Default Hotkeys
        setupDefaultHotkeys();

        logger.info("ApplicationService initialized");
    }

    /**
     * Gibt die Singleton-Instanz zurück.
     *
     * @return ApplicationService Instanz
     */
    public static synchronized ApplicationService getInstance() {
        if (instance == null) {
            instance = new ApplicationService();
        }
        return instance;
    }

    /**
     * Registriert die Standard-Hotkeys.
     */
    private void setupDefaultHotkeys() {
        try {
            // MVP Hinweis: Die folgenden Keycodes sind Platzhalter
            // Für volle macOS Integration wird JNativeHook oder ähnliches benötigt

            // Keycode für F6
            keyboardListener.registerHotkey(97, HotkeyAction.START_STOP);

            // Keycode für F7
            keyboardListener.registerHotkey(98, HotkeyAction.EMERGENCY_STOP);

            // Keycode für F8
            keyboardListener.registerHotkey(100, HotkeyAction.NEXT_PROFILE);

            logger.info("Default hotkeys registered: F6=Start/Stop, F7=Emergency Stop, F8=Next Profile");
        } catch (Exception e) {
            logger.error("Error setting up default hotkeys", e);
        }
    }

    /**
     * Gibt die ClickEngine zurück.
     *
     * @return ClickEngine Instanz
     */
    public ClickEngine getClickEngine() {
        return clickEngine;
    }

    /**
     * Gibt den KeyboardListener zurück.
     *
     * @return KeyboardListener Instanz
     */
    public KeyboardListener getKeyboardListener() {
        return keyboardListener;
    }

    /**
     * Gibt den ConfigurationManager zurück.
     *
     * @return ConfigurationManager Instanz
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Holt das Default-Profil oder das erste verfügbare Profil.
     *
     * @return das Standard-Profil
     */
    public ClickProfile getDefaultProfile() {
        String defaultId = configurationManager.getDefaultProfileId();
        ClickProfile profile = configurationManager.loadProfile(defaultId);

        if (profile == null) {
            List<ClickProfile> allProfiles = configurationManager.getAllProfiles();
            if (!allProfiles.isEmpty()) {
                profile = allProfiles.get(0);
            } else {
                profile = ClickProfile.createDefault();
                configurationManager.saveProfile(profile);
            }
        }

        return profile;
    }

    /**
     * Holt alle verfügbaren Profile.
     *
     * @return Liste aller Profile
     */
    public List<ClickProfile> getAllProfiles() {
        return configurationManager.getAllProfiles();
    }

    /**
     * Speichert ein Profil.
     *
     * @param profile das zu speichernde Profil
     */
    public void saveProfile(ClickProfile profile) {
        configurationManager.saveProfile(profile);
    }

    /**
     * Löscht ein Profil.
     *
     * @param profileId die ID des zu löschenden Profils
     */
    public void deleteProfile(String profileId) {
        configurationManager.deleteProfile(profileId);
    }

    /**
     * Shutdown - Beendet alle Services
     */
    public void shutdown() {
        logger.info("Shutting down ApplicationService...");

        try {
            clickEngine.stopClicking();
            if (clickEngine instanceof ClickEngineImpl) {
                ((ClickEngineImpl) clickEngine).shutdown();
            }
        } catch (Exception e) {
            logger.error("Error shutting down ClickEngine", e);
        }

        try {
            keyboardListener.stopListening();
            if (keyboardListener instanceof KeyboardListenerImpl) {
                ((KeyboardListenerImpl) keyboardListener).shutdown();
            }
        } catch (Exception e) {
            logger.error("Error shutting down KeyboardListener", e);
        }

        logger.info("ApplicationService shutdown complete");
    }
}

