# Click Roboter - Quick Start Guide

## ⚡ 5-Minuten Setup

### 1. Projekt bauen
```bash
cd /Users/fabian/git/robo
mvn clean package -DskipTests
```

**Output:**
```
[INFO] BUILD SUCCESS
[INFO] Final JAR: target/robo-1.0-SNAPSHOT.jar
```

### 2. App starten
```bash
java -jar target/robo-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
09:42:50,123 [main] INFO org.example.robo.Main - === Click Roboter Application Starting ===
09:42:50,456 [main] INFO org.example.robo.service.ApplicationService - ApplicationService initialized
09:42:50,789 [main] INFO org.example.robo.ui.MainWindow - MainWindow initialized
09:42:50,891 [main] INFO org.example.robo.Main - === Click Roboter Application Started ===
09:42:50,891 [main] INFO org.example.robo.Main - Hotkeys configured:
09:42:50,891 [main] INFO org.example.robo.Main - F6 = Start/Stop
09:42:50,891 [main] INFO org.example.robo.Main - F7 = Emergency Stop
09:42:50,891 [main] INFO org.example.robo.Main - F8 = Next Profile
```

### 3. Berechtigungen erteilen (macOS)

Falls Hotkeys nicht funktionieren:

1. **Systemeinstellungen öffnen**
2. **Sicherheit & Datenschutz → Barrierefreiheit**
3. **"Click Roboter" oder "java" zur Liste hinzufügen**

### 4. Erste Automatisierung

**Schritt-für-Schritt:**

1. Fenster öffnen
2. Default Profile ist bereits vorselektiert (10 Hz)
3. Mausposition überprüfen (wird live angezeigt)
4. **"START"** Button drücken oder **F6** Taste
5. App beginnt mit Klicks
6. **"STOP"** Button oder **F7** zum Stoppen

---

## 🎮 Praktische Anwendungsbeispiele

### Beispiel 1: Gaming Auto-Clicker
```
Frequenz: 15 Hz
Position: Mittelpunkt des Game-Fensters
Click-Typ: LEFT
Start: F6 Hotkey
Stop: F7 Notfall-Abbruch
```

### Beispiel 2: Data Entry Automation
```
Frequenz: 3 Hz (langsamer für Sicherheit)
Position: Bestätigungs-Button
Click-Typ: LEFT
Unbegrenzte Klicks bis manuelles Stop
```

### Beispiel 3: Test-Automation
```
Frequenz: 1 Hz (1 Klick pro Sekunde)
Position: UI-Element auf Test-Interface
Click-Typ: RIGHT (Kontext-Menü)
Begrenzte Anzahl oder manueller Stop
```

---

## 📊 Profile Verwenden

### Default Profile speichern
```bash
# Profile wird automatisch in ~/.robo/profiles.json gespeichert
Frequenz: 10 Hz
Position: 500, 400
Click-Typ: LEFT
```

### Neues Profile erstellen (Manual Edit)

1. Datei öffnen: `~/.robo/profiles.json`
2. Neues Profile hinzufügen:

```json
{
  "id": "gaming-fast",
  "name": "Gaming - Very Fast",
  "description": "Für schnelle Spiele",
  "clickFrequency": 25,
  "position": {
    "x": 960,
    "y": 540
  },
  "clickType": "LEFT",
  "numberOfClicks": -1,
  "delayBetweenClicks": 0,
  "createdAt": "2026-02-17T10:00:00Z",
  "lastModified": "2026-02-17T10:00:00Z"
}
```

3. Datei speichern
4. App neustarten
5. Neues Profil sollte in der ComboBox sichtbar sein

---

## 🔧 Troubleshooting

### Problem: "App startet nicht"
```bash
# Java Version überprüfen
java -version
# Sollte: openjdk 17.x oder höher sein

# Falls nicht vorhanden:
# macOS: brew install openjdk@17
```

### Problem: "Hotkeys funktionieren nicht"
1. Accessibility-Berechtigung überprüfen (siehe Schritt 3 oben)
2. Terminal neustarten nach Permission-Änderung
3. App neustarten

### Problem: "Klicks finden nicht statt"
1. **Accessibility überprüfen**: SystemPreferences → Security & Privacy → Accessibility
2. **Mausposition überprüfen**: Live-Display sollte Position zeigen
3. **Logs überprüfen**: `cat ~/.robo/logs/robo.log`

### Problem: "App ist sehr langsam"
1. Frequenz reduzieren (max 100 Hz)
2. Andere Apps im Hintergrund schließen
3. macOS neustarten

---

## 📝 Wichtige Dateien

| Datei | Zweck |
|-------|-------|
| `~/.robo/profiles.json` | Gespeicherte Profile |
| `~/.robo/logs/robo.log` | Application Logs |
| `target/robo-1.0-SNAPSHOT.jar` | Ausführbares JAR |
| `SPEC.md` | Detaillierte Specification |
| `README.md` | Vollständige Dokumentation |

---

## ⌨️ Hotkeys Referenz

| Taste | Funktion | Bearbeiten |
|-------|----------|-----------|
| **F6** | Start/Stop Toggle | ApplicationService.setupDefaultHotkeys() |
| **F7** | Emergency Stop | ApplicationService.setupDefaultHotkeys() |
| **F8** | Next Profile | ApplicationService.setupDefaultHotkeys() |

**Hotkeys anpassen (Entwickler):**
Datei: `src/main/java/org/example/robo/service/ApplicationService.java`
Methode: `setupDefaultHotkeys()`

---

## 🧪 Tests ausführen

```bash
# Alle Tests
mvn test

# Nur bestimmte Test-Klasse
mvn test -Dtest=ClickProfileTest

# Ohne Compilation
mvn test -o
```

**Erwartete Ausgabe:**
```
Tests run: 20, Failures: 0, Errors: 0
```

---

## 📦 Build-Optionen

```bash
# Normal build mit Tests
mvn clean verify

# Schnell build ohne Tests
mvn clean package -DskipTests

# Nur kompilieren
mvn compile

# Nur Tests
mvn test

# Nur Package (bereits kompiliert)
mvn package
```

---

## 🚀 Production-Tipps

### Sichere Start
```bash
# Starten mit hohem Speicher
java -Xmx256m -jar target/robo-1.0-SNAPSHOT.jar

# Mit Debug-Logging
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG \
  -jar target/robo-1.0-SNAPSHOT.jar
```

### Monitoring
```bash
# Logs in real-time folgen
tail -f ~/.robo/logs/robo.log

# Nur Fehler anzeigen
grep ERROR ~/.robo/logs/robo.log

# Statistiken
wc -l ~/.robo/logs/robo.log
```

---

## 🎯 Typische Workflows

### Workflow 1: Einfacher Auto-Clicker
1. App starten
2. Default Profile wählen (10 Hz bereits OK)
3. Mausposition bei Ziel-Element positionieren
4. F6 drücken um zu starten
5. F7 zum Abbrechen

### Workflow 2: Gaming-Optimierung
1. Neues Profile erstellen (30 Hz)
2. Position auf Game-Fenster einstellen
3. Profile speichern
4. Beim Spielen F6/F7 zum Toggle nutzen
5. F8 um zwischen Profilen zu wechseln

### Workflow 3: Data Entry
1. Profile mit 2 Hz erstellen (langsamer)
2. Position auf "OK" Button einstellen
3. Speichern
4. Task starten mit F6
5. Mit F7 pausieren bei Bedarf

---

## 💡 Best Practices

✅ **DO:**
- Häufig Profile speichern für verschiedene Tasks
- Accessibility-Berechtigung vorab geben
- Logs überprüfen bei Problemen
- Häufige Positionen mit niedrigen Frequenzen testen

❌ **DON'T:**
- Mit zu hohen Frequenzen (>50 Hz) beginnen
- Position außerhalb des Screens einstellen
- App ohne Accessibility-Berechtigung nutzen
- Zu lange Sessions ohne Pause (Sicherheit)

---

## 📞 Hilfe & Support

**Logs überprüfen:**
```bash
cat ~/.robo/logs/robo.log | tail -50
```

**Vollständige Dokumentation:**
- `SPEC.md` - Detaillierte Spezifikation
- `README.md` - Ausführliche Dokumentation
- `IMPLEMENTATION_SUMMARY.md` - Entwickler-Info
- `src/main/java/` - Javadoc in Source Code

**Häufige Fragen:**
- F: Funktioniert auf M1/M2 Mac? A: Ja, Java 17 läuft nativ
- F: Kann man die App im Hintergrund verwenden? A: Ja, F6/F7/F8 funktionieren global
- F: Können Profile exportiert werden? A: Ja, Datei ~/.robo/profiles.json kopieren

---

**Viel Erfolg mit Click Roboter! 🚀**

Für Feedback oder Issues: Siehe IMPLEMENTATION_SUMMARY.md

