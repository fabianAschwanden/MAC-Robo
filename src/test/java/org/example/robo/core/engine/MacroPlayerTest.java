package org.example.robo.core.engine;

import org.example.robo.core.profile.Macro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für das MacroPlayer Interface via Test-Implementierung.
 */
class MacroPlayerTest {

    static class StubMacroPlayer implements MacroPlayer {
        private boolean playing = false;

        @Override public void play(Macro macro) { playing = true; }
        @Override public void stop()            { playing = false; }
        @Override public boolean isPlaying()    { return playing; }
    }

    @Test
    void testInitiallyNotPlaying() {
        MacroPlayer player = new StubMacroPlayer();
        assertFalse(player.isPlaying());
    }

    @Test
    void testPlayStartsPlayback() {
        MacroPlayer player = new StubMacroPlayer();
        player.play(new Macro("m1", "Test Macro"));
        assertTrue(player.isPlaying());
    }

    @Test
    void testStopEndsPlayback() {
        MacroPlayer player = new StubMacroPlayer();
        player.play(new Macro("m1", "Test Macro"));
        player.stop();
        assertFalse(player.isPlaying());
    }

    @Test
    void testPlayWithEmptyMacro() {
        MacroPlayer player = new StubMacroPlayer();
        player.play(new Macro());
        assertTrue(player.isPlaying());
    }
}
