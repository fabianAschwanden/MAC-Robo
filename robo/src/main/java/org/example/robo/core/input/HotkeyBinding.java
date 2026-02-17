package org.example.robo.core.input;

/**
 * Representiert eine Hotkey-Bindung mit KeyCode und Modifiern.
 */
public class HotkeyBinding {
    private final int keyCode;
    private final int modifiers; // Kombination von KeyEvent.CTRL_DOWN_MASK, ALT_DOWN_MASK, etc.
    private final HotkeyAction action;

    public HotkeyBinding(int keyCode, int modifiers, HotkeyAction action) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.action = action;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    public HotkeyAction getAction() {
        return action;
    }

    /**
     * Gibt eine lesbare String-Repräsentation der Hotkey-Bindung zurück.
     * Beispiel: "Ctrl+F6", "Shift+Alt+K"
     */
    public String getHotkeyString() {
        StringBuilder sb = new StringBuilder();

        if ((modifiers & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0) {
            sb.append("Ctrl+");
        }
        if ((modifiers & java.awt.event.InputEvent.ALT_DOWN_MASK) != 0) {
            sb.append("Alt+");
        }
        if ((modifiers & java.awt.event.InputEvent.SHIFT_DOWN_MASK) != 0) {
            sb.append("Shift+");
        }
        if ((modifiers & java.awt.event.InputEvent.META_DOWN_MASK) != 0) {
            sb.append("Cmd+");
        }

        // Konvertiere KeyCode zu String
        sb.append(getKeyName());

        return sb.toString();
    }

    private String getKeyName() {
        return java.awt.event.KeyEvent.getKeyText(keyCode);
    }

    @Override
    public String toString() {
        return getHotkeyString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HotkeyBinding)) return false;
        HotkeyBinding other = (HotkeyBinding) obj;
        return keyCode == other.keyCode && modifiers == other.modifiers;
    }

    @Override
    public int hashCode() {
        return keyCode * 31 + modifiers;
    }
}

