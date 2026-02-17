package org.example.robo.config;

import org.example.robo.core.profile.ClickProfile;
import org.example.robo.core.profile.Macro;

import java.util.List;

/**
 * Interface für die Verwaltung von Klick-Profil-Konfigurationen.
 * Handhabt Persistierung und Abruf von Profile.
 */
public interface ConfigurationManager {

    /**
     * Speichert ein Profil.
     *
     * @param profile das zu speichernde Profil
     */
    void saveProfile(ClickProfile profile);

    /**
     * Laden ein Profil nach ID.
     *
     * @param profileId die ID des zu ladenden Profils
     * @return das Profil oder null wenn nicht gefunden
     */
    ClickProfile loadProfile(String profileId);

    /**
     * Holt alle gespeicherten Profile.
     *
     * @return Liste aller Profile
     */
    List<ClickProfile> getAllProfiles();

    /**
     * Löscht ein Profil nach ID.
     *
     * @param profileId die ID des zu löschenden Profils
     */
    void deleteProfile(String profileId);

    /**
     * Holt die ID des Standard-Profils.
     *
     * @return Profil-ID
     */
    String getDefaultProfileId();

    /**
     * Setzt die ID des Standard-Profils.
     *
     * @param profileId die neue Standard-Profil-ID
     */
    void setDefaultProfileId(String profileId);

    // --- Macro persistence API ---
    void saveMacro(Macro macro);
    Macro loadMacro(String macroId);
    java.util.List<Macro> getAllMacros();
    void deleteMacro(String macroId);
}
