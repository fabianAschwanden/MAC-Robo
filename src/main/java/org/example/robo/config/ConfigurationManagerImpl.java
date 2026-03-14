package org.example.robo.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.robo.core.profile.ClickProfile;
import org.example.robo.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation des ConfigurationManager.
 * Verwaltet die Persistierung von Klick-Profilen in JSON-Format.
 */
public class ConfigurationManagerImpl implements ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagerImpl.class);

    private final ObjectMapper objectMapper;
    private final Path configFile;
    private final Path configDir;

    private ProfileConfiguration currentConfig;

    /**
     * Erstellt einen neuen ConfigurationManager.
     */
    public ConfigurationManagerImpl() {
        this.objectMapper = new ObjectMapper();
        // Ignoriere unbekannte Felder in älteren Konfigurationsdateien
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.configDir = Paths.get(Constants.CONFIG_DIR);
        this.configFile = Paths.get(Constants.PROFILES_FILE);

        // Erstelle Config-Verzeichnis falls nicht vorhanden
        ensureConfigDirectory();

        // Lade existierende Konfiguration oder erstelle neue
        loadConfiguration();

        logger.info("ConfigurationManager initialized with config file: {}", configFile);
    }

    @Override
    public void saveProfile(ClickProfile profile) {
        if (profile == null) {
            logger.warn("Cannot save null profile");
            return;
        }

        try {
            // Finde oder füge Profil hinzu
            currentConfig.profiles.removeIf(p -> p.getId().equals(profile.getId()));
            currentConfig.profiles.add(profile);

            // Speichere in Datei
            writeConfigurationToFile();
            logger.info("Profile saved: {}", profile.getName());
        } catch (Exception e) {
            logger.error("Error saving profile", e);
            throw new RuntimeException("Failed to save profile", e);
        }
    }

    @Override
    public ClickProfile loadProfile(String profileId) {
        if (profileId == null) {
            logger.warn("Cannot load profile with null id");
            return null;
        }

        return currentConfig.profiles.stream()
                .filter(p -> p.getId().equals(profileId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ClickProfile> getAllProfiles() {
        return new ArrayList<>(currentConfig.profiles);
    }

    @Override
    public void deleteProfile(String profileId) {
        if (profileId == null) {
            logger.warn("Cannot delete profile with null id");
            return;
        }

        try {
            currentConfig.profiles.removeIf(p -> p.getId().equals(profileId));
            writeConfigurationToFile();
            logger.info("Profile deleted: {}", profileId);
        } catch (Exception e) {
            logger.error("Error deleting profile", e);
            throw new RuntimeException("Failed to delete profile", e);
        }
    }

    // --- Macro persistence implementations ---
    @Override
    public void saveMacro(org.example.robo.core.profile.Macro macro) {
        if (macro == null) {
            logger.warn("Cannot save null macro");
            return;
        }
        try {
            currentConfig.macros.removeIf(m -> m.getId().equals(macro.getId()));
            currentConfig.macros.add(macro);
            writeConfigurationToFile();
            logger.info("Macro saved: {}", macro.getName());
        } catch (IOException e) {
            logger.error("Error saving macro", e);
            throw new RuntimeException("Failed to save macro", e);
        }
    }

    @Override
    public org.example.robo.core.profile.Macro loadMacro(String macroId) {
        if (macroId == null) return null;
        return currentConfig.macros.stream()
                .filter(m -> m.getId().equals(macroId))
                .findFirst().orElse(null);
    }

    @Override
    public java.util.List<org.example.robo.core.profile.Macro> getAllMacros() {
        return new ArrayList<>(currentConfig.macros);
    }

    @Override
    public void deleteMacro(String macroId) {
        if (macroId == null) return;
        try {
            currentConfig.macros.removeIf(m -> m.getId().equals(macroId));
            writeConfigurationToFile();
            logger.info("Macro deleted: {}", macroId);
        } catch (IOException e) {
            logger.error("Error deleting macro", e);
            throw new RuntimeException("Failed to delete macro", e);
        }
    }

    @Override
    public String getDefaultProfileId() {
        return currentConfig.lastUsedProfileId;
    }

    @Override
    public void setDefaultProfileId(String profileId) {
        currentConfig.lastUsedProfileId = profileId;
        try {
            writeConfigurationToFile();
            logger.debug("Default profile id updated to: {}", profileId);
        } catch (IOException e) {
            logger.error("Error updating default profile id", e);
        }
    }

    /**
     * Stellt sicher, dass das Konfigurationsverzeichnis existiert.
     */
    private void ensureConfigDirectory() {
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            logger.error("Error creating config directory", e);
        }
    }

    /**
     * Lädt die Konfiguration aus der Datei oder erstellt eine neue.
     */
    private void loadConfiguration() {
        try {
            if (Files.exists(configFile)) {
                currentConfig = objectMapper.readValue(configFile.toFile(), ProfileConfiguration.class);
                logger.info("Configuration loaded from file");
            } else {
                currentConfig = createDefaultConfiguration();
                writeConfigurationToFile();
                logger.info("New default configuration created");
            }
        } catch (IOException e) {
            logger.error("Error loading configuration, creating new one", e);
            currentConfig = createDefaultConfiguration();
        }
    }

    /**
     * Schreibt die Konfiguration in die Datei.
     */
    private void writeConfigurationToFile() throws IOException {
        // Schreibe erst in temporäre Datei für atomare Operationen
        Path tempFile = Paths.get(configFile + ".tmp");
        objectMapper.writeValue(tempFile.toFile(), currentConfig);

        // Ersetze Original-Datei
        Files.move(tempFile, configFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Erstellt eine neue Standard-Konfiguration.
     */
    private ProfileConfiguration createDefaultConfiguration() {
        ProfileConfiguration config = new ProfileConfiguration();
        config.lastUsedProfileId = Constants.DEFAULT_PROFILE_ID;
        config.profiles = new ArrayList<>();
        config.profiles.add(ClickProfile.createDefault());
        return config;
    }

    /**
     * Interne Klasse für JSON-Struktur.
     */
    public static class ProfileConfiguration {
        public String lastUsedProfileId;
        public List<ClickProfile> profiles;
        public List<org.example.robo.core.profile.Macro> macros;

        public ProfileConfiguration() {
            this.profiles = new ArrayList<>();
            this.macros = new ArrayList<>();
        }
    }
}
