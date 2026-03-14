package org.example.robo.core.engine;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.example.robo.core.profile.ClickType;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper für macOS CoreGraphics Native APIs.
 * Ermöglicht die Kontrolle von Mausklicks über JNA.
 */
public class NativeMacOSAPI {
    private static final Logger logger = LoggerFactory.getLogger(NativeMacOSAPI.class);

    // CoreGraphics Event Types
    private static final int kCGEventLeftMouseDown = 1;
    private static final int kCGEventLeftMouseUp = 2;
    private static final int kCGEventRightMouseDown = 3;
    private static final int kCGEventRightMouseUp = 4;
    private static final int kCGEventMouseMoved = 5;
    private static final int kCGEventScrollWheel = 22;

    // Mouse Button
    private static final int kCGMouseButtonLeft = 0;
    private static final int kCGMouseButtonRight = 1;

    /**
     * JNA Structure für CGPoint (x, y double-Koordinaten)
     */
    public static class CGPoint extends Structure {
        public double x;
        public double y;

        public CGPoint() {
        }

        public CGPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("x", "y");
        }
    }

    /**
     * JNA Interface für CoreGraphics
     */
    private interface CoreGraphics extends Library {
        CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

        Pointer CGEventCreate(Pointer source);
        Pointer CGEventCreateMouseEvent(Pointer source, int type, CGPoint location, int button);
        void CGEventPost(int tap, Pointer event);
        CGPoint CGEventGetLocation(Pointer event);
        void CFRelease(Pointer cf);
    }

    /**
     * Führt einen Mausklick durch.
     *
     * @param x X-Koordinate
     * @param y Y-Koordinate
     * @param clickType Typ des Klicks (LEFT, RIGHT, etc.)
     */
    public static void performMouseClick(int x, int y, ClickType clickType) {
        try {
            logger.debug("Performing {} click at position ({}, {})", clickType, x, y);

            CGPoint location = new CGPoint(x, y);

            switch (clickType) {
                case LEFT -> performLeftClick(location);
                case RIGHT -> performRightClick(location);
                case SCROLL_UP -> performScroll(location, 5);
                case SCROLL_DOWN -> performScroll(location, -5);
                default -> throw new IllegalArgumentException("Unsupported click type: " + clickType);
            }

            logger.debug("Click executed successfully");
        } catch (Exception e) {
            logger.error("Error performing mouse click", e);
            throw new RuntimeException("Failed to perform mouse click", e);
        }
    }

    /**
     * Führt eine reine Mausbewegung aus (ohne Klick).
     *
     * @param x X-Koordinate
     * @param y Y-Koordinate
     */
    public static void performMouseMove(int x, int y) {
        try {
            logger.debug("Moving mouse to ({}, {})", x, y);
            CGPoint location = new CGPoint(x, y);
            Pointer move = CoreGraphics.INSTANCE.CGEventCreateMouseEvent(null, kCGEventMouseMoved, location, 0);
            CoreGraphics.INSTANCE.CGEventPost(0, move);
            CoreGraphics.INSTANCE.CFRelease(move);
        } catch (Exception e) {
            logger.error("Error moving mouse", e);
            throw new RuntimeException("Failed to move mouse", e);
        }
    }

    private static void performLeftClick(CGPoint location) {
        Pointer mouseDown = CoreGraphics.INSTANCE.CGEventCreateMouseEvent(null, kCGEventLeftMouseDown, location, kCGMouseButtonLeft);
        Pointer mouseUp = CoreGraphics.INSTANCE.CGEventCreateMouseEvent(null, kCGEventLeftMouseUp, location, kCGMouseButtonLeft);
        CoreGraphics.INSTANCE.CGEventPost(0, mouseDown);
        CoreGraphics.INSTANCE.CGEventPost(0, mouseUp);
        CoreGraphics.INSTANCE.CFRelease(mouseDown);
        CoreGraphics.INSTANCE.CFRelease(mouseUp);
    }

    private static void performRightClick(CGPoint location) {
        Pointer mouseDown = CoreGraphics.INSTANCE.CGEventCreateMouseEvent(null, kCGEventRightMouseDown, location, kCGMouseButtonRight);
        Pointer mouseUp = CoreGraphics.INSTANCE.CGEventCreateMouseEvent(null, kCGEventRightMouseUp, location, kCGMouseButtonRight);
        CoreGraphics.INSTANCE.CGEventPost(0, mouseDown);
        CoreGraphics.INSTANCE.CGEventPost(0, mouseUp);
        CoreGraphics.INSTANCE.CFRelease(mouseDown);
        CoreGraphics.INSTANCE.CFRelease(mouseUp);
    }

    private static void performScroll(CGPoint location, int direction) {
        Pointer scrollEvent = CoreGraphics.INSTANCE.CGEventCreateMouseEvent(null, kCGEventScrollWheel, location, 0);
        CoreGraphics.INSTANCE.CGEventPost(0, scrollEvent);
        CoreGraphics.INSTANCE.CFRelease(scrollEvent);
    }

    /**
     * Holt die aktuelle Mausposition.
     *
     * @return aktuelle Mausposition
     */
    public static MousePosition getCurrentMousePosition() {
        try {
            Pointer event = CoreGraphics.INSTANCE.CGEventCreate(null);
            CGPoint location = CoreGraphics.INSTANCE.CGEventGetLocation(event);
            CoreGraphics.INSTANCE.CFRelease(event);
            return new MousePosition((int) location.x, (int) location.y);
        } catch (Exception e) {
            logger.error("Error getting current mouse position", e);
            return new MousePosition(0, 0);
        }
    }

    public static boolean hasAccessibilityPermission() {
        try {
            performMouseClick(0, 0, ClickType.LEFT);
            return true;
        } catch (Exception e) {
            logger.warn("Accessibility permission check failed: {}", e.getMessage());
            return false;
        }
    }

    private NativeMacOSAPI() {
    }
}
