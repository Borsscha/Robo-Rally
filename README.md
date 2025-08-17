## How to use our JAR file

### Under the folder `final-jar`:

### 1. Start the server firstly:

``` java -jar server-fat.jar ``` 

### 2. then in new terminal, start client, you need to add some paths to javaFx, especially, because we also have very cool music^^:
```--module-path /Users/lingyin/Java/javafx-sdk-24.0.1/lib --add-modules javafx.controls,javafx.fxml,javafx.media --add-exports=javafx.base/com.sun.javafx=ALL-UNNAMED --add-exports=javafx.base/com.sun.javafx.platform=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.glass.utils=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED```
### 3. In this very long command, only change the part
```/Users/lingyin/Java/javafx-sdk-24.0.1/lib``` into your own path to the library of javaFx
# Aktuellester Zustand (23.Juli)

## 1. UI-Änderungen

- Die Benutzeroberfläche wurde überarbeitet und optisch verbessert.
- Ein Fehler wurde behoben, bei dem bereits ausgewählte Karten erneut ausgewählt werden konnten.
- Ein Timer wird nun im Spiel angezeigt.
- Die Bewegungen der Roboter werden schrittweise visualisiert.
- Die Ausrichtung der Roboter ist nun klar und deutlich sichtbar.
- Die gesamte Darstellung wurde optisch aufgewertet.
- Energy wird in UI angezeigt.
- Verzögertes Ausspielen der Registerkarten.
- Hintergrundmusik wird hinzugefügt.

## 2. Gamelogik-Änderungen
- System.out.print-Ausgaben wurden durch ein Logging-System ersetzt.
- Notwendige Javadoc-Kommentare wurden hinzugefügt.
- Die Funktionen von ConveyorBelt- und Energy-Tiles wurden korrigiert.


### Stand: 13. July
- 1. Neuste Integration mit Zusammenarbeit von GUI und Backend (Version: Als Gegenspieler laufend in Backend): **Branch ``` AI-in-background```**
- 2. Weiterer neue AI Funktionen(Version: AI start von UI mit Logger) sehe Branch **```GarLiz```**

### Branch ```BetaTest``` für aktuelle Zustand (08.July)
Unter the folder beta-test you will see:
- a ```jar```file for server
- a ```jar``` file for client
- a ```README``` file with futher details.

### Stand: 06. July
- Newly Added:
    - All maps can be showed on UI successfully and almost correctly
    - Cards can be played directly in UI
    - Robots' movement can be showed in UI almost correctly
    - More than one game rounds can be played
- To Be Continued:
    - a smarter and more correct KI (KI is not fully correct so that we put it in branch ```ki-feature```separately)
    - correct reboot
    - Animations
    - UpgradeCards to be presented in UI

### Stand: 29.Juni

#### Umgesetzte Features:

- Das **Zusammenarbeit von UI und Backend**:
    - Login
    - LobbyView wechseln
    - Verbindungsverlust(Soft und Hard) checken
    - ReadyStatus setzen
    - Chating und Broadcasting
    - PlayerList auf UI
    - Map wählen, senden, auf UI anzeigen
    - Sichere Fehlerbehandlung
    - Game Starten Json kann erfolgreich an Spieler senden
    - Spielerfeld, Randkarten + Register(``cardTest.java````

**Zu Verbessrung**: Das UI sollte in weiterer Arbeit das Spiel korrekt inizialisieren.

- **Das Backend Implementierung** (auf branch ```Milestone4```): mit der Ideee von Command-driven Development hat Backend folgende Fortschritte:

Spielablauf:

1. Anmelden
   /helloServer <groupName> <isAi>             -> beispiel     /helloServer human false

2. Daten senden
   /playerValues <playerName> <robot>          -> beispiel     /playerValues leaIstSoCool 1

3. Status setzen
   /setStatus <ifReady>                        -> beispiel     /setStatus true

4. Map auswählen
   /selectMap <mapName>                        -> beispiel     /selectMap Dizzy Highway

* Setup Phase *

5. Starting Point wählen
   /setStartingPoint <x> <y>                   -> beispiel     /setStartingPoint 2 3

An dieser Stelle schickt der Server jedem automatisch seine 9. Karten

* Programming Phase *

6. Karten auswählen
   /selectedCard <cardName> <registerIndex>    -> beispiel     /selectedCard MoveI 0

Alle Karten namen:  MoveI, MoveII, MoveIII, PowerUp, Again, TurnLeft, TurnRight, BackUp, UTurn

* Activation Phase *

7. Karte ausspielen
   /playCard <cardName>                        -> beispiel     /playCard MoveI


Der Server verarbeitet die gespielte Karte und aktiviert alle Logics die damit verbunden sind und moved den Roboter auch auf dem
Board wobei auch da jedes mal geprüft wird ob man auf einer SpecialTile steht und aktiviert dort auch effekte.

Es gibt folgende Phasen
- setUpPhase
- programmingPhase
- activationPhase

Nach dem alle Spieler ihre Register gespielt haben, wechselt man wieder in die programmingPhase und dann wieder activationPhase.
Dieser Loop geht solange bis ein Spieler die Siegesbedingung (Checkpoints erreicht) erreicht hat.
