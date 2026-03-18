# MACRobo – Spezifikation

**Stand**: 18. März 2026
**Java**: 21 · **JavaFX**: 21 · **Build**: Maven
**Status**: Produktiv

---

## 1. Projektübersicht

### 1.1 Vision
Eine macOS-Anwendung zur Automatisierung von Mausklicks mit konfigurierbarer Frequenz, Position und Typ für mehrere unabhängige Profile gleichzeitig, erweitert um Browser-Aktionsaufzeichnung via Chrome-Extension.

### 1.2 Zielgruppe
- Gamer (automatische Feuerrate)
- Datenerfasser (wiederholte OK-Klicks)
- QA/Test-Automatisierer (UI-Sequenzen, Web-Workflows)
- Power User (Web-Workflow-Aufzeichnung)

---

## 2. Implementierte Funktionen

### 2.1 Klick-Automatisierung

| ID | Anforderung | Status |
|----|-------------|--------|
| REQ-F001 | Klicks in konfigurierbarer Frequenz ausführen | ✅ |
| REQ-F002 | Klickposition per Maus-Capture (3s-Countdown) übernehmen | ✅ |
| REQ-F002b | Klickposition manuell als X/Y eingeben | ✅ |
| REQ-F003 | Mehrere Klick-Profile speichern | ✅ |
| REQ-F004 | Mehrere Profile gleichzeitig aktivieren (Multi-Profile) | ✅ |
| REQ-F009 | Klickfrequenz in Hz **oder** Intervall in Sekunden einstellen | ✅ |
| REQ-F010 | Klick-Typ wählen: Links, Rechts, Scroll | ✅ |
| REQ-F011 | Anzahl Klicks begrenzen oder unbegrenzt (`-1`) | ✅ |

### 2.2 Tastatur-Steuerung

| ID | Anforderung | Status |
|----|-------------|--------|
| REQ-F006 | F6 = Start/Stop Toggle | ✅ |
| REQ-F007 | F7 = Emergency Stop (sofortiger Abbruch) | ✅ |
| REQ-F008 | Hotkeys über Settings-Dialog anpassbar (Recorder) | ✅ |

> **Hinweis**: F8 (Next Profile) ist im Code registriert, hat aber in der aktuellen UI keine Funktion mehr.

### 2.3 UI

| ID | Anforderung | Status |
|----|-------------|--------|
| REQ-U001 | Dark-Theme Hauptfenster mit Tabellen-Dashboard | ✅ |
| REQ-U002 | Start/Stop-Button | ✅ |
| REQ-U004 | Status-Indikator: Button wechselt zwischen blau/rot | ✅ |
| REQ-U006 | Settings-Dialog mit Tabs (Hotkeys, Click Types, Advanced) | ✅ |
| REQ-U007 | Frequenz-Spinner (1–100 Hz) oder Intervall-Sekunden | ✅ |
| REQ-U008 | Position: Maus-Capture mit Countdown + manuelle Eingabe | ✅ |
| REQ-U009 | Hotkey-Recorder für Start/Stop und Emergency Stop | ✅ |
| REQ-U010 | Profile: Erstellen, Umbenennen, Löschen, Aktivieren/Deaktivieren | ✅ |

### 2.4 Web Capture (Browser-Aufzeichnung)

| ID | Anforderung | Status |
|----|-------------|--------|
| REQ-W001 | Browser-Extension sendet Events an lokalen HTTP-Server (Port 7890) | ✅ |
| REQ-W002 | Web-Aufzeichnungs-Schritte in Tabelle anzeigen | ✅ |
| REQ-W003 | Aufgezeichnete Schritte speichern (`~/.robo/web-macros.json`) | ✅ |
| REQ-W004 | Idle-Threshold: Pausen > 2s werden auf 500ms gekürzt | ✅ |
| REQ-W005 | Schritte manuell löschen | ✅ |

### 2.5 Persistierung

| Datei | Inhalt |
|-------|--------|
| `~/.robo/profiles.json` | Klick-Profile |
| `~/.robo/web-macros.json` | Web-Aufzeichnungen |
| `~/.robo/logs/robo.log` | Rolling Log (10 MB, 7 Tage) |

### 2.6 Nicht-funktionale Anforderungen

| ID | Anforderung | Status |
|----|-------------|--------|
| REQ-N001 | Klick-Timing-Genauigkeit ±10 ms | ✅ |
| REQ-N003 | Stabil bei 100 Hz | ✅ |
| REQ-N004 | Emergency Stop sofort wirksam | ✅ |
| REQ-N006 | Profile persistent gespeichert | ✅ |
| REQ-N007 | Accessibility-Berechtigung erforderlich | ✅ |
| REQ-N008 | Warndialog beim Start wenn Berechtigung fehlt | ✅ |
| REQ-N009 | Keine Telemetrie, keine externe Datenübertragung | ✅ |
| REQ-N010 | Lokale Konfigurationsspeicherung | ✅ |

---

## 3. Nicht implementiert / entfernt

| Feature | Grund |
|---------|-------|
| Menu Bar Integration (REQ-U011–013) | Nicht umgesetzt |
| macOS .app Bundle / DMG-Installer | Nicht umgesetzt, Start via `mvn exec:java` |
| Macro-Playback (Desktop-Makros) | Engine vorhanden (`MacroPlayer`), kein UI-Einstiegspunkt |
| Repeat-Funktion (Forever/Custom) | Entfernt – `numberOfClicks` im Profil übernimmt diese Rolle |
| Live-Mausposition-Anzeige im Hauptfenster | Entfernt |
| F8 Next Profile | Registriert, aber funktionslos in aktueller Tabellenansicht |
| App-Preferences (startMinimized, showNotifications) | Kein UI, Felder im Settings-Dialog nicht verdrahtet |
| Mehrere App-Instanzen verhindern | Kein Singleton-Lock implementiert |

---

## 4. Systemarchitektur

```
┌─────────────────────────────────────────────────────┐
│                      MACRobo                        │
├──────────────────────┬──────────────────────────────┤
│  UI (JavaFX)         │  Web Capture                 │
│  MainWindowFX        │  Browser-Extension           │
│  UIController        │  CaptureServer (HTTP :7890)  │
├──────────────────────┴──────────────────────────────┤
│  ApplicationService (Singleton, Komposition-Root)   │
├──────────────────┬──────────────────────────────────┤
│  ClickEngine     │  KeyboardListener                │
│  Multi-Profile   │  GlobalKeyboardHook (JNA)        │
│  ExecutorService │  F6/F7 Hotkeys                   │
├──────────────────┴──────────────────────────────────┤
│  ConfigurationManager (JSON via Jackson)            │
├─────────────────────────────────────────────────────┤
│  NativeMacOSAPI (CoreGraphics via JNA)              │
│  CGEventCreateMouseEvent · AXIsProcessTrusted       │
└─────────────────────────────────────────────────────┘
```

### 4.1 Technologie-Stack

| Komponente | Technologie |
|-----------|-------------|
| Sprache | Java 21 |
| Build | Maven |
| GUI | JavaFX 21 |
| Maussteuerung | CoreGraphics via JNA (struct-by-value) |
| Tastatur | GlobalKeyboardHook (JNA) |
| Konfiguration | JSON (Jackson 2.16) |
| Logging | SLF4J + Logback 1.4.12 |
| Threading | `ScheduledExecutorService`, `CopyOnWriteArrayList` |

---

## 5. Kern-Interfaces (Ist-Stand)

### ClickEngine
```java
void startClicking(ClickProfile profile);
void startClicking(List<ClickProfile> profiles);
void stopClicking();
void stopClicking(String profileId);
boolean isRunning();
boolean isRunning(String profileId);
void setClickFrequency(int hz);
void setClickPosition(MousePosition position);
MousePosition getCurrentMousePosition();
void addClickEngineListener(ClickEngineListener listener);
void removeClickEngineListener(ClickEngineListener listener);
```

### ConfigurationManager
```java
void saveProfile(ClickProfile profile);
ClickProfile loadProfile(String profileId);
List<ClickProfile> getAllProfiles();
void deleteProfile(String profileId);
String getDefaultProfileId();
void setDefaultProfileId(String profileId);
void saveMacro(Macro macro);
Macro loadMacro(String macroId);
List<Macro> getAllMacros();
void deleteMacro(String macroId);
```

### KeyboardListener
```java
void registerHotkey(int keyCode, HotkeyAction action);
void unregisterHotkey(int keyCode);
void addKeyboardEventListener(KeyboardEventListener listener);
void removeKeyboardEventListener(KeyboardEventListener listener);
```

---

## 6. Datenmodell

### ClickProfile
```json
{
  "id": "default",
  "name": "Default",
  "clickFrequency": 10,
  "speedMode": "FREQUENCY",
  "intervalSeconds": 1,
  "position": { "x": 500, "y": 400 },
  "clickType": "LEFT",
  "numberOfClicks": -1,
  "enabled": true,
  "createdAt": "2026-03-18T10:00:00Z",
  "lastModified": "2026-03-18T10:00:00Z"
}
```

> `numberOfClicks`: `-1` = unbegrenzt, `> 0` = begrenzte Anzahl

### SpeedMode
| Wert | Bedeutung |
|------|-----------|
| `FREQUENCY` | `clickFrequency` Hz (1–100) |
| `INTERVAL` | `intervalSeconds` Sekunden zwischen Klicks |

---

## 7. Web Recording Module

### 7.1 Übersicht

Das Web Recording Module erweitert die Anwendung um Browser-Automatisierung auf Basis von DOM-Events. Statt Mauskoordinaten (die bei responsiven Layouts unzuverlässig sind) werden **CSS-Selektoren** und **XPath-Ausdrücke** gespeichert, um Elemente robust zu identifizieren.

### 7.2 Drei-Phasen-Aufzeichnungsmodell

#### Phase 1 – Capture-Modus
Das System lauscht auf DOM-Events im Browser-Kontext (Chrome Extension). Für jedes Benutzerinteraktionsevent wird nicht die Mausposition, sondern der **CSS-Selektor oder XPath** des Zielelements gespeichert.

#### Phase 2 – Action-Sequencing
Jeder aufgezeichnete Klick wird als `WebRecordingStep` gespeichert:

| Attribut | Beschreibung | Beispiel |
|----------|-------------|---------|
| `eventType` | Art der Interaktion | `CLICK`, `TYPE`, `HOVER` |
| `selector` | Eindeutige Identifikation im DOM | `button[data-id="submit"]` |
| `payload` | Zusätzliche Daten | `"MeinPasswort123"` (bei TYPE) |
| `timingMs` | Zeitabstand zum vorherigen Schritt | `500` |
| `delayType` | Art der Pause | `HARD` oder `SMART` |

#### Phase 3 – Smart Delay
- **HARD**: Feste Wartezeit in Millisekunden
- **SMART**: Warten bis ein Element im DOM geladen/sichtbar ist

### 7.3 Datenmodell: WebRecordingStep

```java
public class WebRecordingStep extends MacroEvent {
    WebEventType eventType;   // CLICK | TYPE | HOVER | NAVIGATE | WAIT
    RobustSelector selector;  // Multi-Selector (CSS + XPath + Text + Relative)
    String payload;           // Eingabe-Text bei TYPE, URL bei NAVIGATE
    long timingMs;            // Effektive Verzögerung zum Vorgänger-Schritt
    DelayType delayType;      // HARD (fest) oder SMART (element-basiert)
}
```

JSON-Serialisierung:
```json
{
  "eventType": "web_step",
  "timestampMs": 1500,
  "webEventType": "CLICK",
  "selector": {
    "cssSelector": "button[data-id='submit']",
    "xpath": "//button[@data-id='submit']",
    "textContent": "Anmelden",
    "relativePosition": null
  },
  "payload": null,
  "timingMs": 500,
  "delayType": "HARD"
}
```

### 7.4 Anforderungen (REQ-W*)

| ID | Beschreibung | Status |
|----|-------------|--------|
| REQ-W001 | Browser-Extension sendet Events an lokalen HTTP-Server (Port 7890) | ✅ |
| REQ-W002 | Web-Aufzeichnungs-Schritte in Tabelle anzeigen | ✅ |
| REQ-W003 | Aufgezeichnete Schritte speichern (`~/.robo/web-macros.json`) | ✅ |
| REQ-W004 | Idle-Threshold: Pausen > 2s werden auf 500ms gekürzt | ✅ |
| REQ-W005 | Schritte manuell löschen | ✅ |
| REQ-W006 | Robust Selector speichert ≥ 2 Strategien pro Step | ✅ |
| REQ-W007 | Idle-Threshold ist in den Einstellungen konfigurierbar | – |
| REQ-W008 | WebRecordingStep ist JSON-serialisierbar (Jackson) | ✅ |
| REQ-W009 | WebMacroRecorder ist thread-safe | ✅ |

---

## 8. Paketstruktur (Ist-Stand)

```
org.example.robo
├── core
│   ├── capture/          CaptureEvent, CaptureServer (Web-HTTP)
│   ├── engine/           ClickEngine, ClickEngineImpl, NativeMacOSAPI,
│   │                     MacroRecorder, MacroPlayer, WebMacroRecorder,
│   │                     TimingController, MouseActuator
│   ├── input/            KeyboardListener, GlobalKeyboardHook,
│   │                     HotkeyAction, HotkeyBinding
│   └── profile/          ClickProfile, ClickType, SpeedMode, DelayType,
│                         Macro, MacroEvent, WebRecordingStep,
│                         RobustSelector, WebEventType
├── config/               ConfigurationManager, ConfigurationManagerImpl
├── service/              ApplicationService
├── ui/
│   ├── MainWindowFX.java
│   ├── UIController.java
│   └── dialog/           SettingsDialog, HotkeyRecorderDialog,
│                         ProfileManagerDialog
└── util/                 Constants, MousePosition
```

---

## 9. Bekannte Einschränkungen

- `mvn exec:java` erforderlich (kein standalone JAR ohne JavaFX-Modul-Setup)
- Accessibility-Berechtigung muss manuell in **Systemeinstellungen → Datenschutz & Sicherheit → Bedienungshilfen** für den Terminal erteilt werden
- Web-Macro-Playback über UI nicht verfügbar (Engine vorhanden, kein UI-Trigger)
- macOS-Kompatibilität: getestet auf Apple Silicon (arm64), Intel via QEMU möglich

---

## 10. Systemanforderungen

| Anforderung | Wert |
|-------------|------|
| macOS | 12.0+ (Monterey) |
| Java | 21 (OpenJDK, Temurin) |
| Architektur | arm64 (nativ), x86_64 (via Rosetta) |
| RAM | min. 256 MB |
| Disk | ~50 MB (ohne Maven-Cache) |

---

## 11. Hotkeys

| Taste | Funktion | Keycode |
|-------|----------|---------|
| F6 | Start/Stop Toggle | 97 |
| F7 | Emergency Stop | 98 |

Konfiguration: `ApplicationService.setupDefaultHotkeys()`

---

## 12. Testing-Strategie

### 12.1 Unit Tests
- ClickEngine: Timing-Genauigkeit, Multi-Profile-Ausführung
- ConfigurationManager: Profil-Speicherung/Laden
- ClickProfile: Validierung, SpeedMode, enabled-Flag

### 12.2 Integration Tests
- Start/Stop-Workflow mit mehreren Einträgen
- "Set pos" Workflow
- Hotkey-Auslösung → Start/Stop

### 12.3 Manuelle Tests
- macOS-Kompatibilität (12.0+)
- Dark Theme auf verschiedenen macOS-Versionen
- Gleichzeitige Klicks an mehreren Positionen

---

## 13. Roadmap

### Phase 1 (MVP) ✅
- [x] ClickEngine mit konfigurierbarer Frequenz
- [x] Tastatur-Hotkey für Start/Stop (F6/F7)
- [x] Profil-System mit Persistenz
- [x] Basis-UI mit JavaFX

### Phase 2: MACRobo UI ✅
- [x] Dark Theme
- [x] Sidebar-Navigation
- [x] Tabellenansicht mit Click-Einträgen
- [x] Multi-Profil gleichzeitige Ausführung
- [x] SpeedMode (FREQUENCY / INTERVAL)
- [x] enabled-Flag pro Eintrag
- [x] "Set pos" inline

### Phase 3: Web Recording ✅
- [x] Browser-Capture-Modus (DOM-Events via Chrome Extension)
- [x] Action-Sequencing mit WebRecordingStep-Datenmodell
- [x] Smart Delay (HARD vs. SMART)
- [x] Robust Selector Engine (CSS + XPath + Text)
- [x] Idle-Threshold (Pausen > 2s werden auf 500ms gekürzt)

### Phase 4: Polish & Distribution
- [ ] Web-Macro-Playback UI
- [ ] Code Signing & Notarization
- [ ] DMG-Installer
- [ ] Singleton-Lock (nur eine App-Instanz)

---

## 14. Glossar

| Begriff | Definition |
|---------|-----------|
| **ClickProfile** | Konfigurationseinheit mit Frequenz, Position, Typ |
| **SpeedMode** | Steuerung via Hz (FREQUENCY) oder Sekunden (INTERVAL) |
| **Emergency Stop** | F7 – sofortiger Abbruch aller aktiven Klicks |
| **Web Capture** | Browser-gestützte Aufzeichnung von Web-Interaktionen |
| **CaptureServer** | Lokaler HTTP-Server auf Port 7890 für Browser-Extension |
| **RobustSelector** | Multi-Selector-Engine (CSS + XPath + Textinhalt) für stabile DOM-Identifikation |
| **Accessibility** | macOS-Berechtigung zur Steuerung von Mausereignissen |

---

**Autor**: Fabian Aschwanden
