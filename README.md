# MACRobo - macOS Click Automation & Web Recorder

Eine macOS-Anwendung für automatisierte Mausklicks und Browser-Aktionsaufzeichnung mit Chrome-Extension-Integration.

## Features

**Click Automatisierung**
- Automatische Mausklicks mit konfigurierbarer Frequenz (1–100 Hz) oder Intervall (z. B. alle 4 Sek.)
- Feste oder dynamische Mausposition (aktuelle Position)
- Mehrere Profile gleichzeitig aktiv

**Web Capture (Browser-Integration)**
- Aufzeichnung von Klicks und Eingaben im Chrome-Browser
- Echtzeit-Übertragung via lokalem HTTP-Server (Port 7890)
- Smart Delay: überlange Pausen werden automatisch auf 500 ms gekürzt
- Robuste Selektoren: CSS, XPath, Text-Content – mehrere Strategien parallel

**Profil- & Makro-Management**
- Profile als JSON unter `~/.robo/profiles.json`
- Web-Makros unter `~/.robo/web-macros.json`
- Maus-Makros aufnehmen und abspielen

**Hotkeys (global, konfigurierbar)**
- Start/Stop, Emergency Stop, Web Capture Toggle

---

## Systemanforderungen

- macOS 11.0 oder neuer
- Java 21
- Google Chrome (für Web Capture)
- Accessibility-Berechtigung (Systemeinstellungen → Datenschutz & Sicherheit → Bedienungshilfen)

---

## Installation & Start

```bash
# Aus dem Quellcode bauen und starten
mvn clean package
mvn javafx:run
```

---

## Web Capture – Einrichtung

Die Web-Capture-Funktion zeichnet Klicks und Tastatureingaben im Chrome-Browser auf und überträgt sie live in die App.

### Schritt 1: App starten

```bash
mvn javafx:run
```

### Schritt 2: Chrome Extension laden

1. Chrome öffnen und zu `chrome://extensions` navigieren
2. Oben rechts **Entwicklermodus** aktivieren
3. **"Entpackte Erweiterung laden"** klicken
4. Den Ordner `browser-extension/` im Projektverzeichnis auswählen
5. Die Extension "MACRobo Web Capture" erscheint in der Liste

### Schritt 3: Aufzeichnung starten

1. In der App auf das **Browser-Icon** in der linken Seitenleiste klicken → Web Capture Panel öffnet sich
2. **"Aufzeichnung starten"** klicken
   - Der eingebettete HTTP-Server startet auf `localhost:7890`
   - Status wechselt zu: `● Server aktiv – localhost:7890`
3. Im Chrome-Browser zur Ziel-Webseite navigieren
4. Die Extension zeigt einen **grünen Punkt** wenn die Verbindung aktiv ist

### Schritt 4: Aktionen aufzeichnen

Alle folgenden Aktionen auf der Webseite werden automatisch erfasst:

| Aktion | Aufgezeichnet als |
|--------|-------------------|
| Klick auf Link, Button, Input | `CLICK` |
| Text in Eingabefeld eingeben | `TYPE` (mit Wert) |

Jede Aktion erscheint sofort als neue Zeile in der Tabelle in der App.

### Schritt 5: Aufzeichnung stoppen

**"Aufzeichnung stoppen"** in der App klicken. Alle aufgezeichneten Schritte bleiben in der Tabelle sichtbar.

---

## Web Capture – Konfiguration

| Option | Beschreibung | Standard |
|--------|-------------|---------|
| Idle-Threshold | Pausen länger als dieser Wert werden auf 500 ms gekürzt | 2000 ms |
| Capture-Hotkey | Globaler Hotkey zum Starten/Stoppen der Aufzeichnung | – |

**Hotkey konfigurieren:**
1. Im Web Capture Panel auf **"Hotkey setzen"** klicken
2. Gewünschte Taste drücken
3. Hotkey wird global registriert – funktioniert auch wenn die App im Hintergrund ist

---

## Tabellen-Spalten (Web Capture)

| Spalte | Bedeutung |
|--------|-----------|
| # | Laufende Schritt-Nummer |
| Typ | Aktionstyp: CLICK, TYPE, HOVER, NAVIGATE, WAIT |
| Selektor | Primärer CSS-Selektor des Elements |
| Payload | Bei TYPE: eingegebener Text; sonst leer |
| Timing | Pause vor diesem Schritt in ms |
| Delay | HARD = feste Zeit; SMART = warten auf Element (orange) |

---

## Hotkeys (Standard)

| Hotkey | Aktion |
|--------|--------|
| F7 | Emergency Stop (alle Klicks sofort stoppen) |
| Konfigurierbar | Start/Stop Toggle |
| Konfigurierbar | Web Capture Toggle |

---

## Troubleshooting

**Extension-Popup zeigt roten Punkt ("Nicht verbunden")**
- Sicherstellen, dass in der App die Aufzeichnung gestartet wurde
- Der Server läuft nur während einer aktiven Aufzeichnung
- Popup schließen und wieder öffnen

**Keine Schritte werden aufgezeichnet**
- Prüfen ob der Status in der App `● Server aktiv` zeigt
- Sicherstellen, dass die Extension auf der Ziel-Seite aktiv ist (kein `chrome://`-Tab)
- Logs prüfen: `~/.robo/logs/robo.log`

**Hotkeys funktionieren nicht**
- Accessibility-Berechtigung prüfen:
  Systemeinstellungen → Datenschutz & Sicherheit → Bedienungshilfen → App hinzufügen

**App startet nicht**
```bash
java -version   # muss Java 21 zeigen
```

---

## Projektstruktur

```
src/main/java/org/example/robo/
├── core/
│   ├── engine/        # ClickEngine, MacroRecorder, WebMacroRecorder
│   ├── input/         # Keyboard Listener, Hotkeys
│   ├── profile/       # Datenmodelle: ClickProfile, WebRecordingStep, RobustSelector
│   └── capture/       # CaptureServer (HTTP), CaptureEvent
├── config/            # ConfigurationManager, JSON-Persistierung
├── ui/                # JavaFX MainWindow, UIController, Dialoge
├── util/              # Constants
└── Main.java

browser-extension/
├── manifest.json      # Chrome Extension MV3
├── content.js         # Klick-/Eingabe-Listener, sendet an localhost:7890
├── popup.html         # Extension-Popup mit Verbindungsstatus
├── popup.js
└── background.js
```

---

## Konfigurationsdateien

| Datei | Inhalt |
|-------|--------|
| `~/.robo/profiles.json` | Click-Profile (Frequenz, Position, Typ) |
| `~/.robo/web-macros.json` | Aufgezeichnete Web-Makros |
| `~/.robo/logs/robo.log` | Anwendungs-Logs |

---

## Lizenz

Dieses Projekt steht unter der [MIT License](LICENSE).

Copyright (c) 2026 Fabian Aschwanden – fabian.aschwanden@gmail.com
