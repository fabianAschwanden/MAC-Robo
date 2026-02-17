package org.example.robo.core.engine;

import org.example.robo.core.profile.ClickType;
import org.example.robo.core.profile.Macro;
import org.example.robo.core.profile.MacroEvent;
import org.example.robo.core.profile.MouseClickEvent;
import org.example.robo.core.profile.MouseMoveEvent;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Einfacher Macro Player, der die Events in zeitlicher Reihenfolge abspielt.
 */
public class MacroPlayerImpl implements MacroPlayer {
    private static final Logger logger = LoggerFactory.getLogger(MacroPlayerImpl.class);

    private final ScheduledExecutorService executor;
    private volatile boolean playing = false;
    private ScheduledFuture<?> playTask;
    private final MouseActuator actuator;

    public MacroPlayerImpl() {
        this(new DefaultMouseActuator());
    }

    public MacroPlayerImpl(MouseActuator actuator) {
        this.executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "MacroPlayerThread");
            t.setDaemon(true);
            return t;
        });
        this.actuator = actuator;
    }

    @Override
    public synchronized void play(Macro macro) {
        if (playing || macro == null || macro.getEvents().isEmpty()) {
            logger.warn("MacroPlayer: nothing to play or already playing");
            return;
        }
        playing = true;
        List<MacroEvent> events = macro.getEvents();
        long startOffset = events.get(0).getTimestampMs();

        logger.info("Playing macro {} ({} events)", macro.getName(), events.size());

        // Starte einen Task, der sequentiell die Events abarbeitet
        playTask = executor.schedule(() -> {
            long playStart = System.currentTimeMillis();
            for (MacroEvent ev : events) {
                if (!playing) break;
                long targetTime = playStart + (ev.getTimestampMs() - startOffset);
                long delay;
                while ((delay = targetTime - System.currentTimeMillis()) > 0) {
                    try {
                        Thread.sleep(Math.min(delay, 50));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (!playing) break;

                if (ev instanceof MouseMoveEvent mve) {
                    MousePosition p = mve.getPosition();
                    actuator.move(p.getX(), p.getY());
                } else if (ev instanceof MouseClickEvent mce) {
                    MousePosition p = mce.getPosition();
                    ClickType ct = mce.getClickType();
                    actuator.click(p.getX(), p.getY(), ct);
                }
            }
            playing = false;
            logger.info("Macro playback finished");
        }, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        if (!playing) return;
        playing = false;
        if (playTask != null) {
            playTask.cancel(true);
            playTask = null;
        }
        logger.info("Macro playback stopped");
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    public void shutdown() {
        stop();
        executor.shutdownNow();
    }

    // Default implementation delegating to native API
    private static class DefaultMouseActuator implements MouseActuator {
        @Override
        public void move(int x, int y) {
            NativeMacOSAPI.performMouseMove(x, y);
        }

        @Override
        public void click(int x, int y, ClickType type) {
            NativeMacOSAPI.performMouseClick(x, y, type);
        }
    }
}
