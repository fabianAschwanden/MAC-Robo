package org.example.robo.core.input;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntConsumer;

/**
 * Globaler Keyboard-Hook via macOS CGEventTap (CoreGraphics).
 * Empfängt Tastaturereignisse system-weit, unabhängig vom App-Fokus.
 *
 * Benötigt Accessibility-Berechtigung:
 *   Systemeinstellungen > Datenschutz & Sicherheit > Eingabehilfen
 *
 * Die gemeldeten Key-Codes sind macOS Virtual Key Codes (z.B. F6=97, F7=98, F8=100).
 */
public class GlobalKeyboardHook {
    private static final Logger logger = LoggerFactory.getLogger(GlobalKeyboardHook.class);

    // CGEventTap-Konstanten
    private static final int  kCGSessionEventTap          = 1;
    private static final int  kCGHeadInsertEventTap        = 0;
    private static final int  kCGEventTapOptionListenOnly  = 1;
    private static final int  kCGEventKeyDown              = 10;
    private static final long kCGEventMaskKeyDown          = 1L << kCGEventKeyDown;
    private static final int  kCGKeyboardEventKeycode      = 9;

    // ── JNA-Interfaces ──────────────────────────────────────────────────────────

    public interface CGEventTapCallback extends Callback {
        Pointer invoke(Pointer proxy, int type, Pointer event, Pointer userInfo);
    }

    private interface CoreGraphicsExt extends Library {
        CoreGraphicsExt INSTANCE = Native.load("CoreGraphics", CoreGraphicsExt.class);

        Pointer CGEventTapCreate(int tap, int place, int options, long eventsOfInterest,
                                 CGEventTapCallback callback, Pointer userInfo);
        long    CGEventGetIntegerValueField(Pointer event, int field);
    }

    private interface CoreFoundationLib extends Library {
        CoreFoundationLib INSTANCE = Native.load("CoreFoundation", CoreFoundationLib.class);

        Pointer CFMachPortCreateRunLoopSource(Pointer allocator, Pointer tap, int order);
        Pointer CFRunLoopGetCurrent();
        void    CFRunLoopRun();
        void    CFRunLoopStop(Pointer rl);
        void    CFRunLoopAddSource(Pointer rl, Pointer source, Pointer mode);
    }

    // ── Zustand ─────────────────────────────────────────────────────────────────

    private Pointer             tapPort;
    private Pointer             runLoopRef;
    private CGEventTapCallback  callbackRef; // Verhindert GC des nativen Callbacks
    private Thread              hookThread;
    private volatile boolean    running = false;

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Startet den globalen Keyboard-Hook in einem Daemon-Thread.
     *
     * @param onKeyDown wird mit dem macOS-VK-Code aufgerufen, sobald eine Taste gedrückt wird
     */
    public void start(IntConsumer onKeyDown) {
        if (running) return;

        callbackRef = (proxy, type, event, userInfo) -> {
            if (type == kCGEventKeyDown) {
                int keyCode = (int) CoreGraphicsExt.INSTANCE
                        .CGEventGetIntegerValueField(event, kCGKeyboardEventKeycode);
                onKeyDown.accept(keyCode);
            }
            return event; // event unverändert weiterreichen (listen-only)
        };

        tapPort = CoreGraphicsExt.INSTANCE.CGEventTapCreate(
                kCGSessionEventTap,
                kCGHeadInsertEventTap,
                kCGEventTapOptionListenOnly,
                kCGEventMaskKeyDown,
                callbackRef,
                null
        );

        if (tapPort == null) {
            logger.error("CGEventTap konnte nicht erstellt werden. " +
                    "Bitte Accessibility-Berechtigung erteilen: " +
                    "Systemeinstellungen > Datenschutz & Sicherheit > Eingabehilfen");
            return;
        }

        running = true;
        hookThread = new Thread(() -> {
            try {
                Pointer source = CoreFoundationLib.INSTANCE
                        .CFMachPortCreateRunLoopSource(null, tapPort, 0);
                runLoopRef = CoreFoundationLib.INSTANCE.CFRunLoopGetCurrent();

                // kCFRunLoopDefaultMode ist eine globale CFString-Konstante in CoreFoundation
                Pointer defaultMode = NativeLibrary.getInstance("CoreFoundation")
                        .getGlobalVariableAddress("kCFRunLoopDefaultMode")
                        .getPointer(0);

                CoreFoundationLib.INSTANCE.CFRunLoopAddSource(runLoopRef, source, defaultMode);
                logger.info("Globaler Keyboard-Hook aktiv (CGEventTap)");
                CoreFoundationLib.INSTANCE.CFRunLoopRun(); // blockiert bis stop()
            } catch (Exception e) {
                logger.error("Fehler im globalen Keyboard-Hook Thread", e);
            }
            running = false;
            logger.info("Globaler Keyboard-Hook beendet");
        }, "GlobalKeyboardHook");
        hookThread.setDaemon(true);
        hookThread.start();
    }

    /**
     * Stoppt den Hook und gibt alle Ressourcen frei.
     */
    public void stop() {
        running = false;
        if (runLoopRef != null) {
            CoreFoundationLib.INSTANCE.CFRunLoopStop(runLoopRef);
            runLoopRef = null;
        }
    }

    public boolean isRunning() {
        return running;
    }
}
