package org.example.robo.core.profile;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Basisklasse für aufgezeichnete Macro-Events (MouseMove, MouseClick)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MouseMoveEvent.class, name = "move"),
        @JsonSubTypes.Type(value = MouseClickEvent.class, name = "click")
})
public abstract class MacroEvent {
    private long timestampMs; // Millisekunden seit Start des Macros

    public MacroEvent() {
    }

    public MacroEvent(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }
}

