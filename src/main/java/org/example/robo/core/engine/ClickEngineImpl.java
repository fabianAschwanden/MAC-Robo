package org.example.robo.core.engine;

import org.example.robo.core.profile.ClickProfile;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation des ClickEngine Interfaces.
 * Unterstützt gleichzeitige Ausführung mehrerer Click-Profile.
 */
public class ClickEngineImpl implements ClickEngine {
    private static final Logger logger = LoggerFactory.getLogger(ClickEngineImpl.class);

    private final ScheduledExecutorService executor;
    private final List<ClickEngineListener> listeners;

    // Pro-Profil State: profileId → ScheduledFuture
    private final Map<String, ScheduledFuture<?>> clickTasks = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> clickCounters = new ConcurrentHashMap<>();
    private final Map<String, ClickProfile> activeProfiles = new ConcurrentHashMap<>();

    // Rückwärtskompatibilität: letztes einzeln gestartetes Profil
    private volatile ClickProfile currentProfile;

    public ClickEngineImpl() {
        this.executor = new ScheduledThreadPoolExecutor(10, r -> {
            Thread t = new Thread(r, "ClickEngineThread");
            t.setDaemon(true);
            return t;
        });
        this.listeners = new CopyOnWriteArrayList<>();
        logger.info("ClickEngine initialized (multi-profile)");
    }

    @Override
    public void startClicking(ClickProfile profile) {
        if (profile == null) {
            notifyListeners(l -> l.onError("Profile cannot be null"));
            return;
        }
        startSingleProfile(profile);
        this.currentProfile = profile;
    }

    @Override
    public void startClicking(List<ClickProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            notifyListeners(l -> l.onError("No profiles to start"));
            return;
        }
        for (ClickProfile profile : profiles) {
            if (profile.isEnabled()) {
                startSingleProfile(profile);
            }
        }
        if (!activeProfiles.isEmpty()) {
            notifyListeners(ClickEngineListener::onEngineStarted);
        }
    }

    private void startSingleProfile(ClickProfile profile) {
        String id = profile.getId();
        if (clickTasks.containsKey(id)) {
            logger.warn("Profile {} already running", id);
            return;
        }

        long intervalMs = TimingController.calculateIntervalMs(profile.getClickFrequency());
        if (profile.getSpeedMode() != null) {
            intervalMs = profile.getClickIntervalMs();
        }

        final long finalIntervalMs = intervalMs;
        clickCounters.put(id, new AtomicLong(0));
        activeProfiles.put(id, profile);

        ScheduledFuture<?> task = executor.scheduleAtFixedRate(
                () -> executeClick(profile),
                0,
                finalIntervalMs,
                TimeUnit.MILLISECONDS
        );
        clickTasks.put(id, task);
        logger.info("Started profile '{}' (interval: {} ms)", profile.getName(), finalIntervalMs);
    }

    @Override
    public void stopClicking() {
        List<String> ids = new ArrayList<>(clickTasks.keySet());
        for (String id : ids) {
            cancelTask(id);
        }
        activeProfiles.clear();
        clickCounters.clear();
        logger.info("All click tasks stopped");
        notifyListeners(ClickEngineListener::onEngineStopped);
    }

    @Override
    public void stopClicking(String profileId) {
        cancelTask(profileId);
        activeProfiles.remove(profileId);
        clickCounters.remove(profileId);
        logger.info("Stopped profile {}", profileId);
        if (activeProfiles.isEmpty()) {
            notifyListeners(ClickEngineListener::onEngineStopped);
        }
    }

    private void cancelTask(String id) {
        ScheduledFuture<?> task = clickTasks.remove(id);
        if (task != null) {
            task.cancel(false);
        }
    }

    @Override
    public boolean isRunning() {
        return !clickTasks.isEmpty();
    }

    @Override
    public boolean isRunning(String profileId) {
        return clickTasks.containsKey(profileId);
    }

    @Override
    public void setClickFrequency(int hz) {
        if (currentProfile != null) {
            currentProfile.setClickFrequency(hz);
        }
    }

    @Override
    public void setClickPosition(MousePosition position) {
        if (currentProfile != null) {
            currentProfile.setPosition(position);
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
        }
    }

    @Override
    public void removeClickEngineListener(ClickEngineListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public ClickProfile getCurrentProfile() {
        return currentProfile;
    }

    private void executeClick(ClickProfile profile) {
        String id = profile.getId();
        if (!clickTasks.containsKey(id)) {
            return;
        }

        try {
            AtomicLong counter = clickCounters.get(id);
            if (counter == null) return;

            if (profile.getNumberOfClicks() > 0 && counter.get() >= profile.getNumberOfClicks()) {
                stopClicking(id);
                return;
            }

            NativeMacOSAPI.performMouseClick(
                    profile.getPosition().getX(),
                    profile.getPosition().getY(),
                    profile.getClickType()
            );

            counter.incrementAndGet();
            notifyListeners(l -> l.onClickExecuted(profile.getPosition()));

        } catch (Exception e) {
            logger.error("Error executing click for profile {}", id, e);
            stopClicking(id);
            notifyListeners(l -> l.onError("Click failed for " + profile.getName() + ": " + e.getMessage()));
        }
    }

    private void notifyListeners(ListenerAction action) {
        for (ClickEngineListener listener : listeners) {
            try {
                action.execute(listener);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
    }

    @FunctionalInterface
    private interface ListenerAction {
        void execute(ClickEngineListener listener);
    }

    public void shutdown() {
        stopClicking();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("ClickEngine shutdown complete");
    }
}
