package org.example.robo.core.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.robo.util.Constants;
import org.example.robo.util.MousePosition;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Repräsentiert ein Click-Profil mit all seinen Konfigurationen.
 */
public class ClickProfile {
    private String id;
    private String name;
    private String description;
    private int clickFrequency; // Hz (1-100)
    private MousePosition position; // X, Y Koordinaten
    private ClickType clickType; // LEFT, RIGHT, SCROLL
    private int numberOfClicks; // -1 = unbegrenzt
    private long delayBetweenClicks; // ms
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    /**
     * Default Konstruktor für Jackson Deserialisierung.
     */
    @JsonCreator
    public ClickProfile(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("clickFrequency") int clickFrequency,
            @JsonProperty("position") MousePosition position,
            @JsonProperty("clickType") ClickType clickType,
            @JsonProperty("numberOfClicks") int numberOfClicks,
            @JsonProperty("delayBetweenClicks") long delayBetweenClicks,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("lastModified") LocalDateTime lastModified) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null ? name : "Unnamed Profile";
        this.description = description != null ? description : "";
        this.clickFrequency = clickFrequency > 0 ? clickFrequency : Constants.DEFAULT_FREQUENCY_HZ;
        this.position = position != null ? position : new MousePosition(Constants.DEFAULT_MOUSE_X, Constants.DEFAULT_MOUSE_Y);
        this.clickType = clickType != null ? clickType : ClickType.LEFT;
        this.numberOfClicks = numberOfClicks;
        this.delayBetweenClicks = delayBetweenClicks;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastModified = lastModified != null ? lastModified : LocalDateTime.now();

        validateProfile();
    }

    /**
     * No-Argument Constructor für UI-basierte Profil-Erstellung.
     */
    public ClickProfile() {
        this(null, null, null, 0, null, null, -1, 0, null, null);
    }

    /**
     * Erstellt ein neues Profil mit Defaults.
     */
    public static ClickProfile createDefault() {
        return new ClickProfile(
                Constants.DEFAULT_PROFILE_ID,
                "Default Profile",
                "Standard Profil",
                Constants.DEFAULT_FREQUENCY_HZ,
                new MousePosition(Constants.DEFAULT_MOUSE_X, Constants.DEFAULT_MOUSE_Y),
                ClickType.LEFT,
                -1,
                Constants.DEFAULT_DELAY_MS,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Validiert das Profil und korrigiert ungültige Werte.
     */
    public void validateProfile() {
        // Frequenz-Validierung
        if (this.clickFrequency < Constants.MIN_FREQUENCY_HZ) {
            this.clickFrequency = Constants.MIN_FREQUENCY_HZ;
        } else if (this.clickFrequency > Constants.MAX_FREQUENCY_HZ) {
            this.clickFrequency = Constants.MAX_FREQUENCY_HZ;
        }

        // Verzögerung-Validierung
        if (this.delayBetweenClicks < 0) {
            this.delayBetweenClicks = 0;
        }

        // numberOfClicks: -1 bedeutet unbegrenzt, alles andere sollte > 0 sein
        if (this.numberOfClicks > 0) {
            // OK
        } else {
            this.numberOfClicks = -1; // unbegrenzt
        }
    }

    // Getter & Setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.lastModified = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.lastModified = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lastModified = LocalDateTime.now();
    }

    public int getClickFrequency() {
        return clickFrequency;
    }

    public void setClickFrequency(int clickFrequency) {
        this.clickFrequency = clickFrequency;
        validateProfile();
        this.lastModified = LocalDateTime.now();
    }

    public MousePosition getPosition() {
        return position;
    }

    public void setPosition(MousePosition position) {
        this.position = position;
        this.lastModified = LocalDateTime.now();
    }

    public ClickType getClickType() {
        return clickType;
    }

    public void setClickType(ClickType clickType) {
        this.clickType = clickType;
        this.lastModified = LocalDateTime.now();
    }

    public int getNumberOfClicks() {
        return numberOfClicks;
    }

    public void setNumberOfClicks(int numberOfClicks) {
        this.numberOfClicks = numberOfClicks;
        validateProfile();
        this.lastModified = LocalDateTime.now();
    }

    public long getDelayBetweenClicks() {
        return delayBetweenClicks;
    }

    public void setDelayBetweenClicks(long delayBetweenClicks) {
        this.delayBetweenClicks = delayBetweenClicks;
        validateProfile();
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickProfile profile = (ClickProfile) o;
        return Objects.equals(id, profile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClickProfile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", clickFrequency=" + clickFrequency +
                ", position=" + position +
                ", clickType=" + clickType +
                '}';
    }

    /**
     * Gibt das Klick-Interval in Millisekunden zurück.
     *
     * @return Interval in ms
     */
    public long getClickIntervalMs() {
        return 1000 / Math.max(1, clickFrequency);
    }
}
