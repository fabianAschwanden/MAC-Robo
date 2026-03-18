package org.example.robo.core.capture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptureEvent {
    @JsonProperty("eventType")   public String eventType;   // CLICK, TYPE, HOVER, NAVIGATE
    @JsonProperty("cssSelector") public String cssSelector;
    @JsonProperty("xpath")       public String xpath;
    @JsonProperty("textContent") public String textContent;
    @JsonProperty("payload")     public String payload;     // typed text for TYPE
    @JsonProperty("timingMs")    public long   timingMs;
    @JsonProperty("url")         public String url;
}
