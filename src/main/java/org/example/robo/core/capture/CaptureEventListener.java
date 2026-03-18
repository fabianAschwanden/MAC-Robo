package org.example.robo.core.capture;

@FunctionalInterface
public interface CaptureEventListener {
    void onCaptureEvent(CaptureEvent event);
}
