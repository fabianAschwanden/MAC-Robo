package org.example.robo.core.profile;

import org.example.robo.util.MousePosition;

/**
 * Repräsentiert eine Mausbewegung im Macro.
 */
public class MouseMoveEvent extends MacroEvent {
    private MousePosition position;

    public MouseMoveEvent() {
        super();
    }

    public MouseMoveEvent(long timestampMs, MousePosition position) {
        super(timestampMs);
        this.position = position;
    }

    public MousePosition getPosition() {
        return position;
    }

    public void setPosition(MousePosition position) {
        this.position = position;
    }
}

