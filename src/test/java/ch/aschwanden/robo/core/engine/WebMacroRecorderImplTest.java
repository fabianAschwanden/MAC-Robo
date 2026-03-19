package ch.aschwanden.robo.core.engine;

import ch.aschwanden.robo.core.profile.DelayType;
import ch.aschwanden.robo.core.profile.RobustSelector;
import ch.aschwanden.robo.core.profile.WebEventType;
import ch.aschwanden.robo.core.profile.WebRecordingStep;
import ch.aschwanden.robo.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für WebMacroRecorderImpl.
 * Deckt Aufzeichnungslogik, Idle-Threshold und Thread-Sicherheit ab.
 */
class WebMacroRecorderImplTest {

    private WebMacroRecorderImpl recorder;
    private static final RobustSelector SELECTOR = RobustSelector.ofCss("button#submit");

    @BeforeEach
    void setUp() {
        recorder = new WebMacroRecorderImpl();
    }

    // ── Initialzustand ─────────────────────────────────────────────────────────

    @Test
    void testInitiallyNotRecording() {
        assertFalse(recorder.isRecording());
    }

    @Test
    void testInitiallyNoSteps() {
        assertTrue(recorder.getRecordedSteps().isEmpty());
    }

    @Test
    void testInitiallyNoMacroId() {
        assertNull(recorder.getCurrentMacroId());
        assertNull(recorder.getCurrentMacroName());
    }

    // ── Start / Stop ───────────────────────────────────────────────────────────

    @Test
    void testStartRecordingSetsRecordingTrue() {
        recorder.startRecording("m1", "Test Macro");
        assertTrue(recorder.isRecording());
    }

    @Test
    void testStartRecordingSetsIdAndName() {
        recorder.startRecording("macro-42", "My Macro");
        assertEquals("macro-42", recorder.getCurrentMacroId());
        assertEquals("My Macro", recorder.getCurrentMacroName());
    }

    @Test
    void testStopRecordingSetsRecordingFalse() {
        recorder.startRecording("m1", "Test");
        recorder.stopRecording();
        assertFalse(recorder.isRecording());
    }

    @Test
    void testStartRecordingClearsPreviousSteps() {
        recorder.startRecording("m1", "First");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        assertFalse(recorder.getRecordedSteps().isEmpty());

        recorder.startRecording("m2", "Second");
        assertTrue(recorder.getRecordedSteps().isEmpty());
        assertEquals("m2", recorder.getCurrentMacroId());
    }

    // ── Schritt aufzeichnen ────────────────────────────────────────────────────

    @Test
    void testRecordStepAddsStep() {
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        assertEquals(1, recorder.getRecordedSteps().size());
    }

    @Test
    void testRecordStepIgnoredWhenNotRecording() {
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        assertTrue(recorder.getRecordedSteps().isEmpty());
    }

    @Test
    void testRecordStepPreservesEventType() {
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.TYPE, SELECTOR, "hello");
        assertEquals(WebEventType.TYPE, recorder.getRecordedSteps().get(0).getWebEventType());
    }

    @Test
    void testRecordStepPreservesPayload() {
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.TYPE, RobustSelector.ofCss("input#name"), "Fabian");
        assertEquals("Fabian", recorder.getRecordedSteps().get(0).getPayload());
    }

    @Test
    void testRecordMultipleStepsInOrder() {
        recorder.startRecording("m1", "Multi");
        recorder.recordStep(WebEventType.CLICK,    SELECTOR,                             null);
        recorder.recordStep(WebEventType.TYPE,     RobustSelector.ofCss("input#email"), "a@b.com");
        recorder.recordStep(WebEventType.NAVIGATE, null,                                "https://example.com");

        List<WebRecordingStep> steps = recorder.getRecordedSteps();
        assertEquals(3, steps.size());
        assertEquals(WebEventType.CLICK,    steps.get(0).getWebEventType());
        assertEquals(WebEventType.TYPE,     steps.get(1).getWebEventType());
        assertEquals(WebEventType.NAVIGATE, steps.get(2).getWebEventType());
        assertEquals("a@b.com",             steps.get(1).getPayload());
    }

    // ── Idle-Threshold / Delay-Logik ───────────────────────────────────────────

    @Test
    void testShortPauseProducesHardDelay() {
        recorder.setIdleThresholdMs(5000); // sehr hoch → keine langen Pausen im Test
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        assertEquals(DelayType.HARD, recorder.getRecordedSteps().get(0).getDelayType());
    }

    @Test
    void testLongPauseProducesSmartDelayAndDefaultTiming() throws InterruptedException {
        recorder.setIdleThresholdMs(30); // niedrig → 50ms Pause überschreitet Threshold
        recorder.startRecording("m1", "Test");
        Thread.sleep(60);
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);

        WebRecordingStep step = recorder.getRecordedSteps().get(0);
        assertEquals(DelayType.SMART, step.getDelayType());
        assertEquals(Constants.DEFAULT_STEP_DELAY_MS, step.getTimingMs());
    }

    @Test
    void testDefaultIdleThresholdMatchesConstants() {
        // Standardwert ist 2000ms — ein sofort gesendeter Schritt muss HARD sein
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        assertEquals(DelayType.HARD, recorder.getRecordedSteps().get(0).getDelayType());
    }

    // ── clearSteps ─────────────────────────────────────────────────────────────

    @Test
    void testClearStepsRemovesAllSteps() {
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        recorder.recordStep(WebEventType.TYPE,  SELECTOR, "abc");
        recorder.clearSteps();
        assertTrue(recorder.getRecordedSteps().isEmpty());
    }

    @Test
    void testClearStepsAllowsNewRecording() {
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        recorder.clearSteps();
        recorder.recordStep(WebEventType.TYPE, SELECTOR, "after clear");
        assertEquals(1, recorder.getRecordedSteps().size());
    }

    // ── Unveränderliche Liste ──────────────────────────────────────────────────

    @Test
    void testGetRecordedStepsIsUnmodifiable() {
        recorder.startRecording("m1", "Test");
        recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
        List<WebRecordingStep> steps = recorder.getRecordedSteps();
        assertThrows(UnsupportedOperationException.class, () -> steps.add(null));
    }

    // ── Thread-Sicherheit ──────────────────────────────────────────────────────

    @Test
    void testConcurrentRecordingIsThreadSafe() throws InterruptedException {
        recorder.startRecording("m1", "Thread Safety");

        int threads  = 8;
        int perThread = 25;
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < perThread; j++) {
                    recorder.recordStep(WebEventType.CLICK, SELECTOR, null);
                }
                latch.countDown();
            });
        }

        latch.await();
        pool.shutdown();
        assertEquals(threads * perThread, recorder.getRecordedSteps().size());
    }
}
