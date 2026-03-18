package org.example.robo.core.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Multi-Selector-Ansatz zur robusten Element-Identifikation im DOM.
 *
 * <p>Dynamische Frameworks (React, Angular) generieren instabile IDs wie {@code id="button-4829"}.
 * Diese Klasse speichert mehrere Selektionsstrategien in Prioritätsreihenfolge:
 * <ol>
 *   <li>CSS-Selektor mit stabilen Attributen (data-*, aria-*, semantische Tags)</li>
 *   <li>XPath als struktureller Fallback</li>
 *   <li>Textinhalt des Elements</li>
 *   <li>Relativer Pfad zum nächsten stabilen Eltern-Element</li>
 * </ol>
 * Beim Abspielen wird jede Strategie der Reihe nach probiert bis eine erfolgreich matcht (REQ-W002).
 */
public class RobustSelector {

    private final String cssSelector;
    private final String xpath;
    private final String textContent;
    private final String relativePosition;

    @JsonCreator
    public RobustSelector(
            @JsonProperty("cssSelector") String cssSelector,
            @JsonProperty("xpath") String xpath,
            @JsonProperty("textContent") String textContent,
            @JsonProperty("relativePosition") String relativePosition) {
        this.cssSelector = cssSelector;
        this.xpath = xpath;
        this.textContent = textContent;
        this.relativePosition = relativePosition;
    }

    /** Erstellt einen Selektor nur aus einem CSS-Ausdruck. */
    public static RobustSelector ofCss(String cssSelector) {
        return new RobustSelector(cssSelector, null, null, null);
    }

    /** Erstellt einen Selektor nur aus einem XPath-Ausdruck. */
    public static RobustSelector ofXPath(String xpath) {
        return new RobustSelector(null, xpath, null, null);
    }

    /** Erstellt einen Selektor auf Basis des sichtbaren Textes. */
    public static RobustSelector ofText(String textContent) {
        return new RobustSelector(null, null, textContent, null);
    }

    /**
     * Erstellt einen vollständig befüllten Selektor mit allen Strategien.
     * Empfohlen für maximale Robustheit (REQ-W001: ≥ 2 Strategien).
     */
    public static RobustSelector of(String cssSelector, String xpath, String textContent) {
        return new RobustSelector(cssSelector, xpath, textContent, null);
    }

    /**
     * Gibt den primären Selektor zurück — die erste nicht-null Strategie.
     * Reihenfolge: CSS → XPath → Text → Relativ.
     */
    public String getPrimary() {
        if (cssSelector != null) return cssSelector;
        if (xpath != null) return xpath;
        if (textContent != null) return "//*[contains(text(),'" + textContent + "')]";
        return relativePosition;
    }

    /** Anzahl der befüllten Strategien. */
    public int strategyCount() {
        int count = 0;
        if (cssSelector != null) count++;
        if (xpath != null) count++;
        if (textContent != null) count++;
        if (relativePosition != null) count++;
        return count;
    }

    public String getCssSelector() {
        return cssSelector;
    }

    public String getXpath() {
        return xpath;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getRelativePosition() {
        return relativePosition;
    }

    @Override
    public String toString() {
        return "RobustSelector{primary='" + getPrimary() + "', strategies=" + strategyCount() + "}";
    }
}
