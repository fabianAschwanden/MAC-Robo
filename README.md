# Click Roboter - macOS App (MVP Phase 1)

Eine benutzerfreundliche macOS-Anwendung, die automatisierte Mausklicks mit konfigurierbarer Häufigkeit und Position durchführt. Perfekt für Gaming, Dateneingabe und Testing-Automatisierung.

## 🚀 Features (Phase 1 MVP)

✅ **Click Automatisierung**
- Automatische Mausklicks mit konfigurierbarer Frequenz (1-100 Hz)
- Benutzerdefinierte Klick-Positionen (X, Y Koordinaten)
- Mehrere voreingestellte Profile speichern

✅ **Benutzer-Steuerung**
- Start/Stop via Schaltfläche oder Hotkeys
- Emergency Stop Taste für sofortigen Halt
- Profil-Wechsel

✅ **Status-Anzeige**
- Live-Anzeige der aktuellen Mausposition
- Status-Indikator (ACTIVE / STOPPED)
- Frequenz-Anzeige

✅ **Profil-Management**
- Profile als JSON speichern/laden
- Automatische Persistierung in `~/.robo/profiles.json`

✅ **Recording & Playback (Neu)**
- Aufnahme von Mausbewegungen und Klicks (RECORD Button)
- Abspielen aufgezeichneter Makros (PLAY Button)
- Capture-Click Funktion zum schnellen Aufnehmen eines Klicks

## 🛠 Systemanforderungen

- macOS 11.0 oder neuer
- Java 17 Runtime
- Accessibility-Berechtigung (für globale Hotkeys)

## 📦 Installation

### Aus dem Quellcode bauen:

```bash
cd /Users/fabian/git/robo
mvn clean package
java -jar target/robo-1.0-SNAPSHOT.jar
```

## 🎮 Verwendung

### Hotkeys (Standard):
- **F6**: Start/Stop Toggle
- **F7**: Emergency Stop (sofortige Beendigung)
- **F8**: Zum nächsten Profil wechseln

### Recording & Playback
- RECORD: startet die Aufnahme von Mausbewegungen und Klicks
- CAPTURE CLICK: nimmt die aktuelle Mausposition als Klick auf
- PLAY: spielt das aktuell aufgenommene Macro ab

### Erste Schritte:

1. App starten
2. Standard Profile ist vorgespeichert (10 Hz, Position 500x400)
3. Mausposition einstellen oder Auto-Detect verwenden
4. Klick-Frequenz anpassen (1-100 Hz)
5. "START" Button drücken oder F6 Hotkey verwenden
6. "STOP" Button oder F7 zum Stoppen

## 📋 Projektstruktur

```
src/main/java/org/example/robo/
├── core/
│   ├── engine/        # ClickEngine, Timing, Native APIs
│   ├── input/         # Keyboard Listener, Hotkeys
│   └── profile/       # ClickProfile, ClickType Datenmodelle
├── config/            # ConfigurationManager, JSON Persistierung
├── ui/                # Swing GUI, MainWindow, UIController
├── service/           # ApplicationService (Singleton)
├── util/              # Constants, Utilities
└── Main.java          # Entry Point
```

## 🧪 Testing

```bash
# Unit Tests ausführen
mvn test

# Tests überspringen und nur bauen
mvn package -DskipTests
```

**Test Coverage:**
- ✅ ClickProfile Validierung
- ✅ TimingController Berechnungen
- ✅ MousePosition Utility
- ✅ ConfigurationManager Persistierung

## 🔧 Konfiguration

Profile werden in JSON speichert unter `~/.robo/profiles.json`:

```json
{
  "lastUsedProfileId": "default",
  "profiles": [
    {
      "id": "default",
      "name": "Default Profile",
      "clickFrequency": 10,
      "position": {
        "x": 500,
        "y": 400
      },
      "clickType": "LEFT",
      "numberOfClicks": -1,
      "delayBetweenClicks": 0
    }
  ]
}
```

## 📊 Logging

Logs werden gespeichert in `~/.robo/logs/robo.log`

Log-Level im Code einstellbar via `src/main/resources/logback.xml`

## 🐛 Bekannte Limitierungen (MVP Phase 1)

- Globale Hotkeys benötigen Accessibility-Berechtigung
- Keine Custom Hotkey Konfiguration (wird in Phase 2 hinzugefügt)
- Keine Menu Bar Integration (Phase 2)
- Keine Settings Dialog (Phase 2)
- Nur einfache UI ohne erweiterte Optionen

## 🚧 Roadmap

### Phase 2: Enhancement
- [ ] Menu Bar Integration mit Status-Icon
- [ ] Settings Dialog (Frequenz-Slider, Position-Picker)
- [ ] Tastenkombinations-Recorder
- [ ] Advanced Profile Manager
- [ ] Verschiedene Click-Typen (Right, Scroll)

### Phase 3: Advanced
- [ ] Click-Sequenzen/Makros
- [ ] Recording & Playback
- [ ] Statistiken & Logging
- [ ] Dark Mode

### Phase 4: Distribution
- [ ] Code Signing & Notarization
- [ ] DMG Installer
- [ ] Dokumentation
- [ ] Performance Optimierung

## 📝 Entwicklung nach Spec-Driven Development

Diese App wurde anhand einer detaillierten Specification entwickelt. Siehe `SPEC.md` für:

- Funktionale Requirements (REQ-F001 bis REQ-F015)
- UI/UX Requirements (REQ-U001 bis REQ-U013)
- Non-Functional Requirements (REQ-N001 bis REQ-N013)
- Benutzerflüsse und User Stories
- Systemarchitektur
- API Spezifikation

## 🏗 Architektur

Das Projekt folgt einer layered architecture:

```
┌─────────────────────┐
│   UI Layer (Swing)  │
├─────────────────────┤
│  UI Controller      │
├─────────────────────┤
│ Application Service │
├─────────────────────┤
│ ClickEngine │ Config│
├─────────────────────┤
│  Keyboard Input     │
├─────────────────────┤
│ macOS Native APIs   │
└─────────────────────┘
```

## 🔐 Sicherheit & Datenschutz

- ✅ Keine externe Datenübertragung
- ✅ Konfigurationen lokal in `~/.robo/` gespeichert
- ✅ Accessibility-Berechtigung obligatorisch
- ✅ Open Source (inspizierbar)

## 💡 Tipps & Tricks

**Häufig benötigte Frequenzen:**
- Gaming (Auto-Clicker): 10-20 Hz
- Data Entry: 2-5 Hz
- Test Automation: 1-3 Hz

**Mausposition finden:**
- Bewegen Sie die Maus zur gewünschten Position
- Position wird live in der App angezeigt
- Klick-Button drücken um zu übernehmen

## 🐛 Troubleshooting

**Hotkeys funktionieren nicht:**
1. Prüfe Accessibility-Berechtigung:
   - Systemeinstellungen → Sicherheit & Datenschutz → Barrierefreiheit
   - Füge "Click Roboter" zur Liste hinzu

**Klicks finden nicht statt:**
1. Überprüfe ob Accessibility-Berechtigung erteilt ist
2. Prüfe ob Mausposition korrekt eingestellt ist
3. Logs in `~/.robo/logs/robo.log` überprüfen

**App startet nicht:**
1. Java 17+ muss installiert sein
2. `java -version` zum Überprüfen

## 📄 Lizenz

MIT License - Siehe LICENSE Datei

## 👨‍💻 Autor

@Fabian Aschwanden fabian.aschwanden@gmail.com

**Erstellungsdatum:** 17. Februar 2026  
**App-Version:** 1.0 (MVP)  
**Status:** Production Ready für Phase 1

---

**Für Fragen oder Issues:** Siehe SPEC.md für detaillierte Dokumentation

