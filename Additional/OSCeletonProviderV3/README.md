- [Installation & Kompilieren](#installation-kompilieren)
- [Benutzung & Konfiguration](#benutzung-konfiguration)
	- [Die von KinBAALL aktuell verwendete Grammatik](#die-von-kinbaall-aktuell-verwendete-grammatik)

# OSCeleton -- README

In diesem Abschnitt befinden sich Informationen zum Kompilieren bzw. Installieren sowie zur Benutzung und Konfiguration von OSCeleton.

## Installation & Kompilieren

Zum Kompilieren der für KinBAALL angepassten Version von OSCeleton wird folgende Software benötigt:

1. [Microsoft Visual Studio 2013 oder 2015](https://www.visualstudio.com)
2. [Microsoft Speech SDK 11](https://msdn.microsoft.com/en-us/library/hh362873(v=office.14).aspx) in der gewünschten Bittigkeit
3. [Kinect for Windows SDK 2.0](https://www.microsoft.com/en-us/download/details.aspx?id=44561)

Danach kann OSCeleton in Visual Studio kompiliert werden. Zu beachten ist dabei, dass alle installierten SDKs und Runtimes die Bittigkeit haben müssen, die auch beim Kompilieren verwendet wird.

Zum Ausführen wird zusätzlich folgende Software benötigt:

1. [Kinect for Windows SDK 2.0](https://www.microsoft.com/en-us/download/details.aspx?id=43662) Language Packs in der gewünschten Sprache
2. [Microsoft Speech Runtime 11](https://www.microsoft.com/en-us/download/details.aspx?id=27225) in der passenden Bittigkeit (selbst wenn das Speed SDK schon installiert ist)


## Benutzung & Konfiguration

OSCeleton wird durch einfachen Doppelklick auf die `/OSCeleton/bin/x86/Release/OSCeleton-KinectSDK2.exe` gestartet. Sollen IP-Adresse und Port konfiguriert werden, müssen die Kommandozeilen-Parameter `oscPort PORT` und `oscHost IP` übergeben werden. Standardwerte sind `7110` und `127.0.0.1`.

In der Mitte des Fensters befindet sich das Echtzeitbild der Kamera, in dem alle erkannten Skelette eingeblendet werden (die Farbe wird zufällig zugewiesen). Farbige Punkte repräsentieren die Skelette und deren Tracking-Status: rot für nicht erkannte, gelb für vermutete und grün für erkannte Gelenke. Die Farbe der Kreise um die Hände gibt deren Status (wird derzeit von KinBAALL nicht verwendet) an: rot für eine geschlossene Hand, grün für eine geöffnete Hand und blau für eine Hand in "Lasso"-Haltung -- die Hand ist geschlossen, aber Zeige- und Mittelfinger sind ausgestreckt. Oben rechts im Fenster erscheinen die erkannten Sprachbefehle, ganz unten links lässt sich der aktuelle Status von OSCeleton ablesen.

OSCeleton nutzt das offizielle Kinect for Windows SDK von Microsoft und läuft daher nur unter Windows 8, 8.1 und 10. Allerdings können KinBAALL und OSCeleton natürlich auf verschiedenen Computern laufen.

Die Kinect kann im Betrieb jederzeit ein- und ausgestöpselt werden.

Unten links im Fenster können die Spracherkennung, das Gesichtstracking (wird von KinBAALL derzeit nicht unterstützt) und die Anzeige des Skeletts (de)aktiviert werden. Unten rechts kann zwischen verschiedenen Kamerabildern gewechselt werden (etwa Infrarot oder RGB).

Die vollständige Grammatik für die Spracherkennung findet sich in der folgenden Tabelle (das Hotword ist vor jedem Befehl nötig, wenn nicht anders angegeben). Diese Grammatik kann in der Datei `OSCeleton/SpeechGrammar.xml` angepasst und erweitert werden, allerdings muss dann neu kompiliert werden.

### Die von KinBAALL aktuell verwendete Grammatik

| **ID** | **Befehl(e)** | **Bedeutung** |
| -------- | -------- | -------- |
| `SWITCH` | "Anschalten", "Ausschalten", "Anmachen", "Ausmachen", "Schalten", "Gerät schalten" | Ein Gerät schalten (verwendbar zum Auslösen statt Geste). |
| `CANCEL` | "Abbrechen", "Abbruch" | Den aktuellen Vorgang abbrechen. |
| `HERE` | "Hier"`*`, "Hierhin"`*`, "Das da"`*`, "Das hier"`*` | Die Wahl eines Ortes oder Gerätes bestätigen. |
| `YES` | "Ja"`*`, "Ja, bitte"`*` | Positive Antwort auf Ja/Nein-Fragen. |
| `NO` | "Nein"`*`, "Nein, danke"`*` | Negative Antwort auf Ja/Nein-Fragen. |
| `TOUR` | "Tourmodus starten", "Bitte nur mich tracken" | Den Tourmodus starten. |
| `CANCEL_TOUR` | "Tourmodus beenden" | Den Tourmodus wieder beenden |
| `DISCOVER` | "Was ist das?", "Was ist das für ein Gerät?" | Ein Gerät entdecken. |
| `EXPLAIN` | "Ich will mehr wissen", "Erklär' mir das" | Sich ein Gerät erklären lassen. |
| `MEMORY` | "Memory-Modus starten" | Alle Erinnerungen einblenden und deren Abruf ermöglichen. |
| `CANCEL_MEMORY` | "Memory-Modus beenden" | Den Memory-Modus wieder beenden. |
| `VIEW_MEMORY` | "Erinnerungen anzeigen" | Die Erinnerung anzeigen, auf die gerade gezeigt wird. |
| `ADD_APPLIANCE` | "Ein neues Gerät hinzufügen" | Ein neues Gerät hinzufügen. |
| `ADD_VIRTUAL_SLIDER` | "Einen virtuellen Schieberegler hinzufügen" | Einen virtuellen Schieberegler hinzufügen. |
| `ADD_VIRTUAL_BUTTON` | "Einen virtuellen Schalter hinzufügen" | Einen virtuellen Schalter hinzufügen. |
| `ADD_MEMORY` | "Eine neue Erinnerung hinzufügen" | Eine neue Erinnerung hinzufügen. |
| `WHEELCHAIR_SEND` | "Rollstuhl dahin" | Den Rollstuhl an den Ort schicken, auf den gerade gezeigt wird. |
| `WHEELCHAIR_SUMMON` | "Rollstuhl zu mir" | Den Rollstuhl zu sich rufen. |

`*` kein Hotword nötig.
