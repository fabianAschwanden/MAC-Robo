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
 */
class ConfigurationManagerTest {

    private ConfigurationManager configManager;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        // Hinweis: Die echte Implementation verwendet ~/.robo/
        // Für Tests müssen wir ein Mock oder Temp-Dir verwenden
        configManager = new ConfigurationManagerImpl();
    }

    @Test
    void testSaveAndLoadProfile() {
        ClickProfile profile = ClickProfile.createDefault();
        profile.setName("Test Profile");

        configManager.saveProfile(profile);
        ClickProfile loaded = configManager.loadProfile(profile.getId());

        assertNotNull(loaded);
        assertEquals(profile.getName(), loaded.getName());
    }

    @Test
    void testGetAllProfiles() {
        List<ClickProfile> profiles = configManager.getAllProfiles();
        assertNotNull(profiles);
        assertTrue(profiles.size() > 0);
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
        ClickProfile profile = new ClickProfile(
                "delete-test-123",
                "To Delete",
                "",
                10,
                null,
                null,
                -1,
                0,
                null,
                null
        );

        configManager.saveProfile(profile);
        assertNotNull(configManager.loadProfile("delete-test-123"));

        configManager.deleteProfile("delete-test-123");
        assertNull(configManager.loadProfile("delete-test-123"));
    }
}

