package ch.aschwanden.robo.core.engine;

import ch.aschwanden.robo.core.profile.ClickType;

/**
 * Abstraktion über native Maus-Operationen, damit der Player testbar ist.
 */
public interface MouseActuator {
    void move(int x, int y);
    void click(int x, int y, ClickType type);
}

