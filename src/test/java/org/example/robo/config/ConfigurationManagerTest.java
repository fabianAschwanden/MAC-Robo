package org.example.robo.config;

import org.example.robo.core.profile.ClickProfile;
import org.example.robo.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für ConfigurationManager.
 * Jeder Test arbeitet in einem isolierten Temp-Verzeichnis (ACID-konform).
 * Es werden keine echten Benutzerdaten (~/.robo/) berührt.
 */
class ConfigurationManagerTest {

    @TempDir
    Path tempDir;

    private ConfigurationManager configManager;

    @BeforeEach
    void setUp() {
        Path configDir  = tempDir.resolve("config");
        Path configFile = configDir.resolve("profiles.json");
        configManager = new ConfigurationManagerImpl(configDir, configFile);
    }

    @Test
    void testSaveAndLoadProfile() {
        ClickProfile profile = ClickProfile.createDefault();
        profile.setName("Test Profile");

        configManager.saveProfile(profile);
        ClickProfile loaded = configManager.loadProfile(profile.getId());

        assertNotNull(loaded);
        assertEquals("Test Profile", loaded.getName());
    }

    @Test
    void testGetAllProfilesContainsDefault() {
        List<ClickProfile> profiles = configManager.getAllProfiles();
        assertNotNull(profiles);
        assertEquals(1, profiles.size(), "Neues Config-Verzeichnis enthält genau das Default-Profil");
        assertEquals(Constants.DEFAULT_PROFILE_ID, profiles.get(0).getId());
    }

    @Test
    void testDefaultProfileExists() {
        ClickProfile defaultProfile = configManager.loadProfile(Constants.DEFAULT_PROFILE_ID);
        assertNotNull(defaultProfile);
        assertEquals(Constants.DEFAULT_PROFILE_ID, defaultProfile.getId());
    }

    @Test
    void testSetAndGetDefaultProfileId() {
        ClickProfile profile = ClickProfile.createDefault();
        profile.setId("test-profile-123");
        configManager.saveProfile(profile);

        configManager.setDefaultProfileId("test-profile-123");
        assertEquals("test-profile-123", configManager.getDefaultProfileId());
    }

    @Test
    void testDeleteProfile() {
        ClickProfile profile = ClickProfile.createDefault();
        profile.setId("delete-test-123");
        profile.setName("To Delete");

        configManager.saveProfile(profile);
        assertNotNull(configManager.loadProfile("delete-test-123"));

        configManager.deleteProfile("delete-test-123");
        assertNull(configManager.loadProfile("delete-test-123"));
    }
}
