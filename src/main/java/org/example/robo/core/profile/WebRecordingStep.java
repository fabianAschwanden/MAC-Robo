package org.example.robo.core.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ein einzelner aufgezeichneter Schritt im Web-Automatisierungs-Ablauf.
 *
 * <p>Erweitert {@link MacroEvent} und wird in der JSON-Persistenz als Subtyp
 * {@code "web_step"} gespeichert. Im Gegensatz zu {@link MouseClickEvent} (das
 * Bildschirmkoordinaten nutzt) identifiziert WebRecordingStep das Ziel-Element
 * über einen {@link RobustSelector} im DOM.
 *
 * <p>Beispiel (JSON):
 * <pre>{@code
 * {
 *   "eventType": "web_step",
 *   "timestampMs": 1500,
 *   "webEventType": "CLICK",
 *   "selector": { "cssSelector": "button[data-id='submit']", "xpath": "//button[@data-id='submit']" },
 *   "payload": null,
 *   "timingMs": 500,
 *   "delayType": "HARD"
 * }
 * }</pre>
 */
public class WebRecordingStep extends MacroEvent {

    private final WebEventType webEventType;
    private final RobustSelector selector;
    /** Zusätzliche Nutzdaten: Eingabe-Text bei TYPE, Ziel-URL bei NAVIGATE. */
    private final String payload;
    /** Effektive Verzögerung zum vorherigen Schritt in Millisekunden (nach Idle-Threshold-Kürzung). */
    private final long timingMs;
    /** Ob die Pause fest (HARD) oder element-basiert (SMART) ist. */
    private final DelayType delayType;

    @JsonCreator
    public WebRecordingStep(
            @JsonProperty("timestampMs") long timestampMs,
            @JsonProperty("webEventType") WebEventType webEventType,
            @JsonProperty("selector") RobustSelector selector,
            @JsonProperty("payload") String payload,
            @JsonProperty("timingMs") long timingMs,
            @JsonProperty("delayType") DelayType delayType) {
        super(timestampMs);
        this.webEventType = webEventType;
        this.selector = selector;
        this.payload = payload;
        this.timingMs = timingMs;
        this.delayType = delayType != null ? delayType : DelayType.HARD;
    }

    public static Builder builder() {
        return new Builder();
    }

    public WebEventType getWebEventType() {
        return webEventType;
    }

    public RobustSelector getSelector() {
        return selector;
    }

    public String getPayload() {
        return payload;
    }

    public long getTimingMs() {
        return timingMs;
    }

    public DelayType getDelayType() {
        return delayType;
    }

    @Override
    public String toString() {
        return String.format("WebRecordingStep{type=%s, selector=%s, timing=%dms, delay=%s}",
                webEventType, selector != null ? selector.getPrimary() : null, timingMs, delayType);
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static class Builder {
        private long timestampMs;
        private WebEventType webEventType;
        private RobustSelector selector;
        private String payload;
        private long timingMs;
        private DelayType delayType = DelayType.HARD;

        public Builder timestamp(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public Builder webEventType(WebEventType webEventType) {
            this.webEventType = webEventType;
            return this;
        }

        public Builder selector(RobustSelector selector) {
            this.selector = selector;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder timing(long timingMs) {
            this.timingMs = timingMs;
            return this;
        }

        public Builder delayType(DelayType delayType) {
            this.delayType = delayType;
            return this;
        }

        public WebRecordingStep build() {
            return new WebRecordingStep(timestampMs, webEventType, selector, payload, timingMs, delayType);
        }
    }
}
