package org.example.robo.core.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert ein aufgezeichnetes Macro (Reihe von MacroEvents).
 */
public class Macro {
    private String id;
    private String name;
    private List<MacroEvent> events = new ArrayList<>();
    private LocalDateTime createdAt = LocalDateTime.now();

    public Macro() {
    }

    public Macro(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MacroEvent> getEvents() {
        return events;
    }

    public void setEvents(List<MacroEvent> events) {
        this.events = events;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

