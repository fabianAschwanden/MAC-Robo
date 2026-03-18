# MACRobo - Quick Start Guide

## ⚡ 5-Minuten Setup

### 1. Projekt bauen
```bash
cd /Users/fabian/git/robo
mvn clean package -DskipTests
```

**Output:**
```
[INFO] BUILD SUCCESS
```

### 2. App starten
```bash
mvn exec:java
```

> **Hinweis:** `java -jar` funktioniert nicht direkt, da JavaFX-Abhängigkeiten
> über Maven verwaltet werden.

**Erwartete Ausgabe:**
```
09:42:50.123 [main] INFO  org.example.robo.Main - === MACRobo Application Starting ===
09:42:50.456 [main] INFO  org.example.robo.service.ApplicationService - ApplicationService initialized
09:42:50.789 [main] INFO  org.example.robo.ui.MainWindowFX - MainWindow initialized
09:42:50.891 [main] INFO  org.example.robo.Main - === MACRobo Application Started ===
09:42:50.891 [main] INFO  org.example.robo.Main - Hotkeys configured:
09:42:50.891 [main] INFO  org.example.robo.Main -   F6          = Start/Stop
09:42:50.891 [main] INFO  org.example.robo.Main -   F7          = Emergency Stop
```

### 3. Berechtigungen erteilen (macOS)

**Wichtig:** Ohne diese Berechtigung werden keine Mausklicks ausgeführt.
Die App zeigt beim Start automatisch einen Warndialog falls die Berechtigung fehlt.

1. **Systemeinstellungen öffnen**
2. **Datenschutz & Sicherheit → Bedienungshilfen**
3. **Terminal (oder die App) zur Liste hinzufügen und aktivieren**
4. **App neu starten**

### 4. Erste Automatisierung

**Schritt-für-Schritt:**

1. App öffnen
2. Default Profile ist bereits in der Tabelle vorhanden (10 Hz)
3. Klick auf **"Set pos"** in der Position-Spalte → 3-Sekunden-Countdown, dann wird die aktuelle Mausposition übernommen
4. Alternativ: **"Enter"** klicken → X/Y-Koordinaten manuell eingeben
5. **"START"** Button drücken oder **F6** Taste
6. App beginnt mit Klicks auf der eingestellten Position
7. **"STOP"** Button oder **F6** erneut zum Stoppen — **F7** für sofortigen Notfall-Abbruch

---

## 🎮 Praktische Anwendungsbeispiele

### Beispiel 1: Gaming Auto-Clicker
```
Frequenz: 15 Hz
Position: Mittelpunkt des Game-Fensters (via "Set pos" erfassen)
Click-Typ: LEFT
Start: F6 Hotkey
Stop: F6 erneut oder F7 für Notfall-Abbruch
```

### Beispiel 2: Data Entry Automation
```
Frequenz: 3 Hz (langsamer für Sicherheit)
Position: Bestätigungs-Button (via "Enter" manuell eingeben)
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
```
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
  "numberOfClicks": 0,
  "createdAt": "2026-02-17T10:00:00Z",
  "lastModified": "2026-02-17T10:00:00Z"
}
```

> `numberOfClicks: 0` = unbegrenzte Klicks

3. Datei speichern
4. App neustarten
5. Neues Profil erscheint in der Tabelle

---

## 🔧 Troubleshooting

### Problem: "App startet nicht"
```bash
# Java Version überprüfen
java -version
# Sollte: openjdk 21.x oder höher sein

# Falls nicht vorhanden:
# macOS: brew install openjdk@21
```

### Problem: "Hotkeys funktionieren nicht"
1. Accessibility-Berechtigung überprüfen (siehe Schritt 3 oben)
2. Terminal neu starten nach Permission-Änderung
3. App neu starten

### Problem: "Klicks finden nicht statt / Klick landet bei (0,0)"
1. **Bedienungshilfen-Berechtigung prüfen**: Systemeinstellungen → Datenschutz & Sicherheit → Bedienungshilfen
2. Beim App-Start erscheint ein Warndialog wenn die Berechtigung fehlt
3. Nach Berechtigung erteilen: App neu starten

### Problem: "Position stimmt nicht"
1. **"Set pos"** Button in der Tabelle verwenden (3s Countdown)
2. Oder **"Enter"** für manuelle X/Y-Eingabe
3. Logs prüfen: `tail -f ~/.robo/logs/robo.log`

### Problem: "App ist sehr langsam"
1. Frequenz reduzieren (max 100 Hz)
2. Andere Apps im Hintergrund schließen
3. macOS neustarten

---

## 📝 Wichtige Dateien

| Datei | Zweck |
|-------|-------|
| `~/.robo/profiles.json` | Gespeicherte Profile |
| `~/.robo/logs/robo.log` | Application Logs (Rolling, max 10 MB, 7 Tage) |
| `target/robo-1.0-SNAPSHOT.jar` | Kompiliertes JAR |
| `README.md` | Vollständige Dokumentation |

---

## ⌨️ Hotkeys Referenz

| Taste | Funktion |
|-------|----------|
| **F6** | Start/Stop Toggle |
| **F7** | Emergency Stop (sofortiger Abbruch) |

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

# Nur kompilieren ohne Tests
mvn compile
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

# App starten (empfohlen)
mvn exec:java
```

---

## 🚀 Production-Tipps

### App starten
```bash
# Standard
mvn exec:java

# Mit Debug-Logging
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG
```

### Monitoring
```bash
# Logs in real-time folgen
tail -f ~/.robo/logs/robo.log

# Nur Fehler anzeigen
grep ERROR ~/.robo/logs/robo.log
```

---

## 🎯 Typische Workflows

### Workflow 1: Einfacher Auto-Clicker
1. App starten
2. Default Profile wählen (10 Hz bereits OK)
3. **"Set pos"** klicken → Maus zur Zielposition bewegen (3s Countdown)
4. F6 drücken um zu starten
5. F6 erneut oder F7 zum Stoppen

### Workflow 2: Gaming-Optimierung
1. Neues Profile in der Tabelle erstellen (30 Hz)
2. **"Enter"** klicken → Koordinaten des Game-Fensters eingeben
3. Profile wird automatisch gespeichert
4. Beim Spielen F6/F7 zum Toggle nutzen

### Workflow 3: Data Entry
1. Profile mit 2 Hz erstellen (langsamer)
2. **"Enter"** → Koordinaten des "OK"-Buttons eingeben
3. Task starten mit F6
4. Mit F6 pausieren bei Bedarf, F7 für sofortigen Abbruch

---

## 💡 Best Practices

✅ **DO:**
- Bedienungshilfen-Berechtigung vorab erteilen
- Position mit **"Enter"** präzise eingeben statt Countdown zu nutzen
- Logs überprüfen bei Problemen (`~/.robo/logs/robo.log`)
- Mit niedrigen Frequenzen testen bevor du erhöhst

❌ **DON'T:**
- Mit zu hohen Frequenzen (>50 Hz) beginnen
- Position außerhalb des Screens einstellen
- App ohne Bedienungshilfen-Berechtigung nutzen
- Zu lange Sessions ohne Pause

---

## 📞 Hilfe & Support

**Logs prüfen:**
```bash
tail -50 ~/.robo/logs/robo.log
```

**Vollständige Dokumentation:**
- `README.md` - Ausführliche Dokumentation
- `src/main/java/` - Javadoc im Source Code

**Häufige Fragen:**
- F: Funktioniert auf M1/M2/M3 Mac? A: Ja, Java 21 läuft nativ auf Apple Silicon
- F: Kann man die App im Hintergrund verwenden? A: Ja, F6/F7 funktionieren global
- F: Können Profile exportiert werden? A: Ja, Datei `~/.robo/profiles.json` kopieren

---

**Viel Erfolg mit MACRobo! 🚀**
