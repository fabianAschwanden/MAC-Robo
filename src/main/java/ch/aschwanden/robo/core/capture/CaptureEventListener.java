package ch.aschwanden.robo.core.capture;

@FunctionalInterface
public interface CaptureEventListener {
    void onCaptureEvent(CaptureEvent event);
}
