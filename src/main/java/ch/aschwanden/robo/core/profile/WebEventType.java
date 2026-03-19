package ch.aschwanden.robo.core.profile;

/**
 * Art der Browser-Interaktion innerhalb eines WebRecordingStep.
 */
public enum WebEventType {
    CLICK("click"),
    TYPE("type"),
    HOVER("hover"),
    NAVIGATE("navigate"),
    WAIT("wait");

    private final String displayName;

    WebEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
