package org.example.robo.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Repräsentiert eine Mausposition mit X und Y Koordinaten.
 */
public class MousePosition {
    private final int x;
    private final int y;

    /**
     * Erstellt eine neue Mausposition.
     *
     * @param x X-Koordinate (Pixel)
     * @param y Y-Koordinate (Pixel)
     */
    @JsonCreator
    public MousePosition(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gibt die X-Koordinate zurück.
     *
     * @return X-Koordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gibt die Y-Koordinate zurück.
     *
     * @return Y-Koordinate
     */
    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MousePosition that = (MousePosition) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "MousePosition{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
