package org.example.robo.core.engine;

import org.example.robo.core.profile.ClickProfile;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implementation des ClickEngine Interfaces.
 * Verwaltet die automatisierte Klick-Simulation mit konfigurierbarer Frequenz und Position.
 */
public class ClickEngineImpl implements ClickEngine {
    private static final Logger logger = LoggerFactory.getLogger(ClickEngineImpl.class);

    private final ScheduledExecutorService executor;
    private final List<ClickEngineListener> listeners;

    private volatile boolean isRunning = false;
    private volatile ClickProfile currentProfile;
    private ScheduledFuture<?> clickTask;
    private long clicksExecuted = 0;

    /**
     * Erstellt eine neue ClickEngine Instanz.
     */
    public ClickEngineImpl() {
        this.executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "ClickEngineThread");
            t.setDaemon(true);
            return t;
        });
        this.listeners = new ArrayList<>();
        logger.info("ClickEngine initialized");
    }

    @Override
    public void startClicking(ClickProfile profile) {
        if (isRunning) {
            logger.warn("Click engine already running");
            notifyListeners(listener -> listener.onError("Engine already running"));
            return;
        }

        if (profile == null) {
            logger.error("Profile cannot be null");
            notifyListeners(listener -> listener.onError("Profile cannot be null"));
            return;
        }

        try {
            this.currentProfile = profile;
            this.clicksExecuted = 0;
            this.isRunning = true;

            // Berechne Intervall in Millisekunden
            long intervalMs = TimingController.calculateIntervalMs(profile.getClickFrequency());

            logger.info("Starting click engine with profile: {} (freq: {} Hz, interval: {} ms)",
                    profile.getName(), profile.getClickFrequency(), intervalMs);

            // Starte den Klick-Task
            this.clickTask = executor.scheduleAtFixedRate(
                    this::executeClick,
                    0,
                    intervalMs,
                    TimeUnit.MILLISECONDS
            );

            notifyListeners(ClickEngineListener::onEngineStarted);
        } catch (Exception e) {
            logger.error("Error starting click engine", e);
            isRunning = false;
            notifyListeners(listener -> listener.onError("Failed to start engine: " + e.getMessage()));
        }
    }

    @Override
    public void stopClicking() {
        if (!isRunning) {
            logger.debug("Click engine already stopped");
            return;
        }

        try {
            isRunning = false;

            if (clickTask != null) {
                clickTask.cancel(false);
                clickTask = null;
            }

            logger.info("Click engine stopped. Total clicks executed: {}", clicksExecuted);
            notifyListeners(ClickEngineListener::onEngineStopped);
        } catch (Exception e) {
            logger.error("Error stopping click engine", e);
            notifyListeners(listener -> listener.onError("Error stopping engine: " + e.getMessage()));
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void setClickFrequency(int hz) {
        if (currentProfile != null) {
            currentProfile.setClickFrequency(hz);
            logger.debug("Click frequency updated to: {} Hz", hz);
        }
    }

    @Override
    public void setClickPosition(MousePosition position) {
        if (currentProfile != null) {
            currentProfile.setPosition(position);
            logger.debug("Click position updated to: {}", position);
        }
    }

    @Override
    public MousePosition getCurrentMousePosition() {
        try {
            return NativeMacOSAPI.getCurrentMousePosition();
        } catch (Exception e) {
            logger.error("Error getting current mouse position", e);
            return new MousePosition(0, 0);
        }
    }

    @Override
    public void addClickEngineListener(ClickEngineListener listener) {
        if (listener != null) {
            listeners.add(listener);
            logger.debug("Listener added: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void removeClickEngineListener(ClickEngineListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logger.debug("Listener removed: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public ClickProfile getCurrentProfile() {
        return currentProfile;
    }

    /**
     * Führt einen einzelnen Klick aus.
     * Diese Methode wird regelmäßig vom ScheduledExecutorService aufgerufen.
     */
    private void executeClick() {
        if (!isRunning || currentProfile == null) {
            return;
        }

        try {
            // Prüfe ob maximale Anzahl von Klicks erreicht wurde
            if (currentProfile.getNumberOfClicks() > 0 && clicksExecuted >= currentProfile.getNumberOfClicks()) {
                stopClicking();
                return;
            }

            // Führe Klick durch
            NativeMacOSAPI.performMouseClick(
                    currentProfile.getPosition().getX(),
                    currentProfile.getPosition().getY(),
                    currentProfile.getClickType()
            );

            clicksExecuted++;

            // Benachrichtige Listener
            notifyListeners(listener -> listener.onClickExecuted(currentProfile.getPosition()));

        } catch (Exception e) {
            logger.error("Error executing click", e);
            isRunning = false;
            notifyListeners(listener -> listener.onError("Click execution failed: " + e.getMessage()));
        }
    }

    /**
     * Benachrichtige alle registrierten Listener mit einer Action.
     *
     * @param action die auf jedem Listener auszuführende Action
     */
    private void notifyListeners(ListenerAction action) {
        for (ClickEngineListener listener : new ArrayList<>(listeners)) {
            try {
                action.execute(listener);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
    }

    /**
     * Funktionales Interface für Listener-Aktionen.
     */
    @FunctionalInterface
    private interface ListenerAction {
        void execute(ClickEngineListener listener);
    }

    /**
     * Beendet den Click Engine und gibt Ressourcen frei.
     */
    public void shutdown() {
        stopClicking();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error shutting down executor", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("ClickEngine shutdown complete");
    }
}

