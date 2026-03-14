# Click Roboter - macOS App Specification

## 1. Projektübersicht

### 1.1 Vision
Eine benutzerfreundliche macOS-Anwendung, die Tastatureingaben in Mausklicks konvertiert und automatisierte Mausklicks mit konfigurierbarer Häufigkeit und Position durchführt.

### 1.2 Zielgruppe
- Gamer
- Datenerfasser
- QA/Test-Automatisierer
- Power User, die wiederholte Klick-Aufgaben automatisieren möchten

### 1.3 Hauptfunktionalität
Die App ermöglicht:
1. **Tastatur-zu-Maus-Konvertierung**: Tastatureingaben in Mausklicks umwandeln
2. **Automatisierte Klicks**: Wiederholte Klicks mit konfigurierbarer Frequenz an festgelegten Positionen
3. **Aufgabenautomatisierung**: Repetitive Aufgaben wie Gaming, Dateneingabe und Testing automatisieren

---

## 2. Funktionale Anforderungen

### 2.1 Grundfunktionen

#### 2.1.1 Klick-Automatisierung
- **REQ-F001**: App soll Mausklicks in konfigurierbarer Frequenz durchführen können
- **REQ-F002**: Benutzer soll Klickposition (X, Y Koordinaten) konfigurieren können
- **REQ-F003**: App soll mehrere voreingestellte Klick-Profile speichern können
- **REQ-F004**: Benutzer soll zwischen verschiedenen Profilen wechseln können
- **REQ-F005**: App soll aktuelle Mausposition anzeigen/auslesen können

#### 2.1.2 Tastatur-Steuerung
- **REQ-F006**: Benutzer soll eine Tastenkombination definieren, um Klick-Automatisierung zu starten/stoppen
- **REQ-F007**: Benutzer soll eine Notfall-Abbruch-Taste (Emergency Stop) definieren können
- **REQ-F008**: Tastatureingaben sollen in Mausklicks an vorkonfigurierter Position konvertiert werden können

#### 2.1.3 Konfigurationsoptionen
- **REQ-F009**: Benutzer soll Klickfrequenz in Hz oder ms pro Klick einstellen können
- **REQ-F010**: Benutzer soll Mausclick-Typ wählen können (Linksklick, Rechtsklick, Scroll)
- **REQ-F011**: Benutzer soll die Anzahl der zu klickenden Wiederholungen definieren können (optional unbegrenzt)
- **REQ-F012**: Benutzer soll Verzögerungen zwischen Klicks einstellen können

#### 2.1.4 Anwendungsbeispiele
- **REQ-F013**: Gaming: Automatische Feuerrate-Simulation bei gehaltenem Tastendruck
- **REQ-F014**: Dateneingabe: Automatische wiederholte Klicks auf OK/Bestätigung-Button
- **REQ-F015**: Testing: Automatisierte Klick-Sequenzen auf UI-Elemente

### 2.2 UI/UX-Anforderungen

#### 2.2.1 Hauptfenster
- **REQ-U001**: Benutzerfreundliches Dashboard mit Status-Anzeige
- **REQ-U002**: Start/Stop-Button für Klick-Automatisierung
- **REQ-U003**: Live-Anzeige der aktuellen Mausposition
- **REQ-U004**: Status-Indikator (aktiv/inaktiv)
- **REQ-U005**: Anzeige der aktuellen Klickfrequenz

#### 2.2.2 Einstellungen/Konfiguration
- **REQ-U006**: Einstellungs-Dialog mit Tabs für verschiedene Konfigurationsbereiche
- **REQ-U007**: Klick-Frequenz-Slider/Input-Feld (Range: 1-100 Hz)
- **REQ-U008**: Position-Picker: Benutzer kann aktuelle Position übernehmen oder koordinaten eingeben
- **REQ-U009**: Tastenkombinations-Recorder für Start/Stop und Emergency Stop
- **REQ-U010**: Profil-Verwaltung: Speichern, Laden, Löschen von Profilen

#### 2.2.3 Status/Tray
- **REQ-U011**: macOS Menu Bar Icon mit Status-Anzeige
- **REQ-U012**: Quick-Toggle für Start/Stop aus dem Menu Bar
- **REQ-U013**: Schneller Zugriff zu letztem genutztem Profil

### 2.3 Nicht-funktionale Anforderungen

#### 2.3.1 Performance
- **REQ-N001**: Klicks sollen mit Genauigkeit von ±10ms durchgeführt werden
- **REQ-N002**: App soll minimale CPU-Last verursachen (< 5% im Idle)
- **REQ-N003**: App soll bei hohen Klickfrequenzen (100 Hz) stabil laufen

#### 2.3.2 Zuverlässigkeit
- **REQ-N004**: Emergency Stop soll sofort funktionieren
- **REQ-N005**: App soll nach Fehler automatisch wiederherstellen können
- **REQ-N006**: Konfigurationen sollen persistent gespeichert werden

#### 2.3.3 Sicherheit & Datenschutz
- **REQ-N007**: App benötigt Accessibility-Berechtigung unter macOS
- **REQ-N008**: Benutzer muss Zugriff explizit genehmigen
- **REQ-N009**: Keine Telemetrie oder externe Datenübertragung
- **REQ-N010**: Konfigurationen werden lokal gespeichert

#### 2.3.4 Benutzerfreundlichkeit
- **REQ-N011**: App soll mit macOS 11.0+ kompatibel sein
- **REQ-N012**: Intuitive Bedienung ohne Dokumentation möglich
- **REQ-N013**: Hilfe und Tooltips für alle Features

---

## 3. Systemarchitektur

### 3.1 Komponenten

```
┌─────────────────────────────────────────────┐
│         macOS Click Roboter App             │
├─────────────────────────────────────────────┤
│                                               │
│  ┌──────────────┐  ┌──────────────────┐    │
│  │     UI       │  │  Menu Bar        │    │
│  │  (JavaFX)    │  │  Integration     │    │
│  └──────┬───────┘  └────────┬─────────┘    │
│         │                    │               │
│  ┌──────┴────────────────────┴────────┐    │
│  │      Application Controller        │    │
│  │  - State Management                │    │
│  │  - Configuration Manager           │    │
│  └──────┬────────────────────┬───────┘    │
│         │                    │             │
│  ┌──────┴──────────┐  ┌──────┴─────────┐ │
│  │  Click Engine   │  │  Keyboard      │ │
│  │  - Threading    │  │  Listener      │ │
│  │  - Timing       │  │  - Hotkeys     │ │
│  └──────┬──────────┘  └────────────────┘ │
│         │                                  │
│  ┌──────┴──────────────────────────────┐ │
│  │   macOS Native APIs                  │ │
│  │  - CGEventCreate (Mouse)             │ │
│  │  - CGEventPost (Event Dispatch)      │ │
│  │  - NSEvent (Keyboard)                │ │
│  │  - Accessibility Framework           │ │
│  └──────────────────────────────────────┘ │
│                                            │
└────────────────────────────────────────────┘
```

### 3.2 Technologie-Stack

| Komponente | Technologie |
|-----------|-------------|
| Programmiersprache | Java 17+ |
| Build Tool | Maven |
| GUI Framework | JavaFX 21.0.2 |
| Mouse Control | CoreGraphics (via JNA) |
| Keyboard Input | JNativeHook oder AWT KeyListener |
| Config Storage | JSON (Jackson) |
| macOS Integration | JNI/JNA für native APIs |
| Threading | Java ExecutorService |
| Logging | SLF4J + Logback |

---

## 4. Datenbankschema (Persistierung)

### 4.1 Profil-Speicherung
```json
{
  "profiles": [
    {
      "id": "gaming_profile_1",
      "name": "Gaming - Fast Click",
      "description": "10 Klicks pro Sekunde am Mausposition",
      "clickFrequency": 10,
      "position": {
        "x": 500,
        "y": 400
      },
      "clickType": "LEFT",
      "numberOfClicks": -1,
      "delayBetweenClicks": 0,
      "enabled": true,
      "createdAt": "2026-02-17T10:00:00Z",
      "lastModified": "2026-02-17T10:00:00Z"
    }
  ],
  "hotkeys": {
    "startStop": "F6",
    "emergencyStop": "F7",
    "toggleProfile": "F8"
  },
  "preferences": {
    "startMinimized": false,
    "showMenuBar": true,
    "enableNotifications": true
  }
}
```

---

## 5. API-Spezifikation

### 5.1 Core Interfaces

#### 5.1.1 ClickEngine
```java
public interface ClickEngine {
  void startClicking(ClickProfile profile);
  void stopClicking();
  boolean isRunning();
  void setClickFrequency(int hz);
  void setClickPosition(Point position);
  MousePointer getCurrentMousePosition();
}
```

#### 5.1.2 KeyboardListener
```java
public interface KeyboardListener {
  void registerHotkey(KeyCombination key, HotkeyAction action);
  void unregisterHotkey(KeyCombination key);
  void addKeyboardEventListener(KeyboardEventListener listener);
  void removeKeyboardEventListener(KeyboardEventListener listener);
}
```

#### 5.1.3 ConfigurationManager
```java
public interface ConfigurationManager {
  void saveProfile(ClickProfile profile);
  ClickProfile loadProfile(String profileId);
  List<ClickProfile> getAllProfiles();
  void deleteProfile(String profileId);
  void savePreferences(AppPreferences preferences);
  AppPreferences loadPreferences();
}
```

#### 5.1.4 ClickProfile
```java
public class ClickProfile {
  private String id;
  private String name;
  private String description;
  private int clickFrequency; // Hz
  private Point position; // X, Y Koordinaten
  private ClickType clickType; // LEFT, RIGHT, SCROLL
  private int numberOfClicks; // -1 = unbegrenzt
  private long delayBetweenClicks; // ms
  private LocalDateTime createdAt;
  private LocalDateTime lastModified;
  // Getter/Setter...
}
```

---

## 6. Benutzerflüsse (User Stories)

### 6.1 Hauptszenarios

#### US-001: Schnelle Automatisierung starten
**Als** Gamer
**Möchte** ich schnell einen vorgestellten Klick-Roboter mit meiner Tastenkombination starten
**Damit** ich meine Gaming-Performance verbessern kann

**Akzeptanzkriterien:**
- Benutzer drückt eingestellte Hotkey (z.B. F6)
- App startet automatische Klicks mit gespeicherter Konfiguration
- Menu Bar Icon zeigt "aktiv"-Status an
- Benutzer kann sofort stoppen mit Emergency Stop (F7)

#### US-002: Neues Profil erstellen
**Als** Power User
**Möchte** ich ein neues Klick-Profil mit spezifischen Einstellungen erstellen
**Damit** ich verschiedene Aufgaben mit unterschiedlichen Konfigurationen automatisieren kann

**Akzeptanzkriterien:**
- Dialog zum Erstellen neuer Profile
- Klickfrequenz einstellen (1-100 Hz)
- Mausposition wählen (Auto-Detect aktuelle Position oder Manual Input)
- Profil speichern und benennen
- In Profil-Liste sichtbar

#### US-003: Tastenkombination konfigurieren
**Als** Benutzer
**Möchte** ich Custom Hotkeys definieren
**Damit** ich die App nach meinen Bedürfnissen steuern kann

**Akzeptanzkriterien:**
- Einstellungs-Dialog für Hotkeys
- Tastenkombinations-Recorder
- Konflikt-Erkennung bei doppelten Keys
- Start/Stop und Emergency Stop separat konfigurierbar

#### US-004: Datenerfassung automatisieren
**Als** Datenerfasser
**Möchte** ich repetitive Klicks auf OK/Bestätigung-Buttons automatisieren
**Damit** ich effizienter arbeiten und Zeit sparen kann

**Akzeptanzkriterien:**
- Profil mit niedriger, konstanter Klickfrequenz
- Feste Position für OK-Button
- Unbegrenzte Klicks bis manueller Stop
- Start/Stop aus Menu Bar

---

## 7. Fehlerbehandlung & Edge Cases

### 7.1 Fehlerszenarien

| Szenario | Verhalten |
|----------|-----------|
| Accessibility-Berechtigung nicht erteilt | User-freundliche Warnung + Link zu Systemeinstellungen |
| Mouse Position außerhalb des Screens | Warnung, Klicks werden nicht ausgeführt |
| Zu hohe Klickfrequenz (>100 Hz) | Warnung, Frequenz wird begrenzt auf 100 Hz |
| App verliert Focus | Klicks stoppen automatisch (Sicherheit) |
| macOS schläft | App wird pausiert, Resume nach Aufwachen |
| Mehrere Instanzen laufen | Warnung, nur eine Instanz erlaubt |

---

## 8. Testing-Strategie

### 8.1 Unit Tests
- ClickEngine: Klick-Timing-Genauigkeit
- ConfigurationManager: Profil-Speicherung/Laden
- KeyboardListener: Hotkey-Erkennung
- ClickProfile: Validierung

### 8.2 Integration Tests
- Start/Stop-Workflow
- Profil-Wechsel unter Lastzustand
- Tastatur-Events zur Klick-Ausführung

### 8.3 UI Tests
- Dialog-Funktionalität
- Menu Bar-Integration
- Status-Anzeige

### 8.4 Manuelle Tests
- macOS-Kompatibilität (11.0+)
- Accessibility-Integration
- Emergency Stop unter Last

---

## 9. Deployment & Installation

### 9.1 Packaging
- JAR-Bundle mit eingebetteten Abhängigkeiten
- macOS .app Bundle (Code Signing optional)
- DMG-Installer für Distribution

### 9.2 Systemanforderungen
- macOS 11.0 oder neuer
- Java 17 Runtime (falls nicht gebündelt)
- 50 MB Festplatte
- 256 MB RAM

### 9.3 Installation
1. DMG-Download
2. Drag & Drop in Applications-Ordner
3. First-Run: Accessibility-Berechtigung erteilen
4. App nutzbar

---

## 10. Roadmap & Phasen

### Phase 1 (MVP): Core Functionality
- [ ] Basic ClickEngine mit configurierbarer Frequenz
- [ ] Tastatur-Hotkey für Start/Stop
- [ ] Einfaches Profil-System
- [ ] Basis-UI mit Start/Stop-Button
- [ ] Mausposition-Anzeige

### Phase 2: Enhancement
- [ ] Menu Bar Integration
- [ ] Tastenkombinations-Recorder
- [ ] Profile Manager UI
- [ ] Verschiedene Click-Typen (Left, Right, Scroll)
- [ ] Emergency Stop-Taste

### Phase 3: Advanced Features
- [ ] Click-Sequenzen/Makros
- [ ] Recording und Playback
- [ ] Zeitbasierte Automatisierung
- [ ] Advanced Logging/Statistiken
- [ ] Dunkler Modus

### Phase 4: Polish & Distribution
- [ ] Code Signing & Notarization
- [ ] DMG-Installer
- [ ] Umfassende Dokumentation
- [ ] Performance-Optimierung

---

## 11. Metriken & Success Criteria

### 11.1 Funktionale Metriken
- Click-Timing-Genauigkeit: ±10ms
- CPU-Auslastung im Idle: < 5%
- Startup-Zeit: < 3 Sekunden
- Memory Footprint: < 100 MB

### 11.2 Benutzer-Metriken
- Ease of Use: Neue Benutzer sollten ohne Dokumentation ein Profil erstellen können
- Profile-Management: < 2 Minuten zum Wechsel zwischen Profilen
- Hotkey-Response: < 100ms von Tastendruck zu erstem Klick

---

## 12. Glossar

| Begriff | Definition |
|---------|-----------|
| **Click Robot** | Software-Agent, der automatisierte Mausklicks durchführt |
| **Profile** | Gespeicherte Konfiguration mit Klickfrequenz, Position, etc. |
| **Hotkey** | Tastenkombination zum Steuern der App |
| **Emergency Stop** | Notfall-Taste zum sofortigen Stopp aller Klicks |
| **Click Frequency** | Anzahl der Klicks pro Sekunde (Hz) |
| **Accessibility Framework** | macOS API zur Kontrolle von UI-Elementen |

---

## 13. Anhang: Code Structure

### 13.1 Geplante Paketstruktur
```
org.example.robo
├── core
│   ├── engine
│   │   ├── ClickEngine.java
│   │   ├── ClickEngineImpl.java
│   │   ├── TimingController.java
│   │   ├── MacroRecorder.java
│   │   └── MacroPlayer.java
│   ├── input
│   │   ├── KeyboardListener.java
│   │   ├── KeyboardEventListener.java
│   │   └── HotkeyAction.java
│   └── profile
│       ├── ClickProfile.java
│       ├── ClickType.java
│       └── Macro.java
├── config
│   ├── ConfigurationManager.java
│   ├── ConfigurationManagerImpl.java
│   └── AppPreferences.java
├── ui
│   ├── MainWindowFX.java      (JavaFX-basierte UI)
│   ├── UIController.java      (MVC-Controller)
│   ├── SettingsDialog.java    (Geplant)
│   ├── ProfileManager.java    (Geplant)
│   └── MenuBarIntegration.java (Geplant)
├── service
│   ├── ProfileService.java
│   ├── ApplicationService.java
│   └── MacroService.java
├── util
│   ├── MousePositionDetector.java
│   ├── Constants.java
│   └── MousePosition.java
└── Main.java
```

---

**Letzter Update**: 17. Februar 2026 (JavaFX Migration abgeschlossen)
**Autor**: Fabian Aschwanden
**Status**: JavaFX Implementation Active

