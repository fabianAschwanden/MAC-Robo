package org.example.robo.core.profile;

import org.example.robo.core.profile.ClickType;
import org.example.robo.util.MousePosition;

/**
 * Repräsentiert einen Mausklick im Macro.
 */
public class MouseClickEvent extends MacroEvent {
    private MousePosition position;
    private ClickType clickType;

    public MouseClickEvent() {
        super();
    }

    public MouseClickEvent(long timestampMs, MousePosition position, ClickType clickType) {
        super(timestampMs);
        this.position = position;
        this.clickType = clickType;
    }

    public MousePosition getPosition() {
        return position;
    }

    public void setPosition(MousePosition position) {
        this.position = position;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public void setClickType(ClickType clickType) {
        this.clickType = clickType;
    }
}

