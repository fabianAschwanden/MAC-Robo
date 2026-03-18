package org.example.robo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für MousePosition.
 */
class MousePositionTest {

    @Test
    void testMousePositionCreation() {
        MousePosition pos = new MousePosition(100, 200);
        assertEquals(100, pos.getX());
        assertEquals(200, pos.getY());
    }

    @Test
    void testMousePositionEquality() {
        MousePosition pos1 = new MousePosition(100, 200);
        MousePosition pos2 = new MousePosition(100, 200);
        MousePosition pos3 = new MousePosition(150, 250);

        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
    }

    @Test
    void testMousePositionHashCode() {
        MousePosition pos1 = new MousePosition(100, 200);
        MousePosition pos2 = new MousePosition(100, 200);

        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    void testMousePositionToString() {
        MousePosition pos = new MousePosition(100, 200);
        assertTrue(pos.toString().contains("100"));
        assertTrue(pos.toString().contains("200"));
    }
}

