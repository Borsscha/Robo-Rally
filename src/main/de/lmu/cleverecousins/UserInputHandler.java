package de.lmu.cleverecousins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.protocol.cheats.CheatMoveBody;
import de.lmu.cleverecousins.protocol.cheats.CheatMoveMessage;
import de.lmu.cleverecousins.protocol.cheats.CheatTurnBody;
import de.lmu.cleverecousins.protocol.cheats.CheatTurnMessage;
import de.lmu.cleverecousins.protocol.message.*;
import de.lmu.cleverecousins.protocol.messageBody.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw user input (slash commands and admin cheats) into concrete protocol message objects
 * and serializes them to JSON strings.
 * <p>
 * Supported commands include (examples):
 * <ul>
 *   <li><code>/helloServer &lt;group&gt; &lt;isAI&gt;</code></li>
 *   <li><code>/playerValues &lt;name&gt; &lt;robotID&gt;</code></li>
 *   <li><code>/setStatus &lt;true|false&gt;</code></li>
 *   <li><code>/sendChat &lt;toClientID&gt; &lt;text...&gt;</code></li>
 *   <li><code>/selectMap &lt;mapName&gt;</code></li>
 *   <li><code>/setStartingPoint &lt;x&gt; &lt;y&gt;</code></li>
 *   <li><code>/selectedCard &lt;cardName&gt; &lt;register&gt;</code></li>
 *   <li><code>/rebootDirection &lt;top|right|bottom|left&gt;</code></li>
 *   <li><code>/playCard &lt;cardName&gt;</code></li>
 *   <li><code>/pickDamage &lt;card1&gt; [&lt;card2&gt; ...]</code></li>
 *   <li><code>#move &lt;steps&gt;</code> (cheat)</li>
 *   <li><code>#turn &lt;left|right|u&gt;</code> (cheat)</li>
 * </ul>
 * If an unknown command is encountered or arguments are missing, an {@link IllegalArgumentException}
 * is thrown.
 */
public class UserInputHandler {

    /** Jackson mapper used to serialize message objects to JSON. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a raw command line (e.g. from a text field) to a JSON protocol string.
     *
     * @param rawInput full user-entered line including the leading command token
     * @return JSON string representing the corresponding protocol message
     * @throws JsonProcessingException if serialization fails
     * @throws IllegalArgumentException if the command is malformed or missing required args
     */
    public String processInput(String rawInput) throws JsonProcessingException {
        String[] parts = rawInput.trim().split("\\s+", 2);
        String command = parts[0];
        switch (command) {
            case "/helloServer": {
                // /helloServer groupName ifUsingAI
                String[] helloParts = rawInput.trim().split("\\s+", 3);
                if (helloParts.length < 3) {
                    throw new IllegalArgumentException("Usage: /helloServer <group> <isAI>");
                }
                String groupName = helloParts[1];
                boolean isAI = Boolean.parseBoolean(helloParts[2]);
                HelloServerBody body = new HelloServerBody(groupName, isAI, "Version 0.1");
                HelloServerMessage message = new HelloServerMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/playerValues": {
                // /playerValues <playerName> <robotID>
                String[] playerValuesParts = rawInput.trim().split("\\s+", 3);
                if (playerValuesParts.length < 3) {
                    throw new IllegalArgumentException("Usage: /playerValues <playerName> <robotTD>");
                }
                String playerName = playerValuesParts[1];
                int robotID = Integer.parseInt(playerValuesParts[2]);
                PlayerValuesBody body = new PlayerValuesBody(playerName, robotID);
                PlayerValuesMessage message = new PlayerValuesMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/setStatus": {
                // /setStatus <ifReady>
                String[] statusParts = rawInput.trim().split("\\s+", 2);
                if (statusParts.length < 2) {
                    throw new IllegalArgumentException("Usage: /setStatus <playerName> <robotTD>");
                }
                boolean ready = Boolean.parseBoolean(statusParts[1]);
                SetStatusBody body = new SetStatusBody(ready);
                SetStatusMessage message = new SetStatusMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/sendChat": {
                // /sendChat <to> <messsage>
                Pattern pattern = Pattern.compile("^/sendChat\\s+(\\S+)\\s+(.+)$");
                Matcher matcher = pattern.matcher(rawInput);
                if (matcher.matches()) {
                    int to = Integer.parseInt(matcher.group(1));
                    String message = matcher.group(2);
                    SendChatBody body = new SendChatBody(message, to);
                    SendChatMessage jsonChat = new SendChatMessage(body);
                    return objectMapper.writeValueAsString(jsonChat);
                }
            }

            case "/selectMap": {
                // /selectMap <mapName>
                String[] mapParts = rawInput.trim().split("\\s+", 2);
                if (mapParts.length < 2) {
                    throw new IllegalArgumentException("Usage: /selectMap <mapName>");
                }
                String map = mapParts[1];
                MapSelectedBody body = new MapSelectedBody(map);
                MapSelectedMessage message = new MapSelectedMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/setStartingPoint": {
                // /setStartingPoint <x> <y>
                String[] startingPointParts = rawInput.trim().split("\\s+");
                if (startingPointParts.length < 3) {
                    throw new IllegalArgumentException("Usage: /setStartingPoint <x> <y>");
                }
                int x = Integer.parseInt(startingPointParts[1]);
                int y = Integer.parseInt(startingPointParts[2]);
                SetStartingPointBody body = new SetStartingPointBody(x, y);
                SetStartingPointMessage message = new SetStartingPointMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/selectedCard": {
                // /selectedCard <cardName> <register>
                String[] slectedCardParts = rawInput.trim().split("\\s+");
                if (slectedCardParts.length < 3) {
                    throw new IllegalArgumentException("Usage: /selectedCard <cardName> <register>");
                }
                String card = slectedCardParts[1];
                int register = Integer.parseInt(slectedCardParts[2]);
                SelectedCardBody body = new SelectedCardBody(card, register);
                SelectedCardMessage message = new SelectedCardMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/rebootDirection": {
                // /rebootDirection <top|right|bottom|left>
                String[] rebootDirectionParts = rawInput.trim().split("\\s+");
                if (rebootDirectionParts.length < 2) {
                    throw new IllegalArgumentException("Usage: /rebootDirection <top|right|bottom|left>");
                }
                String direction = rebootDirectionParts[1];
                RebootDirectionBody body = new RebootDirectionBody(direction);
                RebootDirectionMessage message = new RebootDirectionMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/playCard": {
                // /playCard <cardName>
                String[] playCardParts = rawInput.trim().split("\\s+", 2);
                if (playCardParts.length < 2) {
                    throw new IllegalArgumentException("Usage: /playCard <cardName>");
                }
                String cardName = playCardParts[1];

                PlayCardBody body = new PlayCardBody(cardName);
                PlayCardMessage message = new PlayCardMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "/pickDamage": {
                // /pickDamage <card1> <card2> ...
                String[] damageParts = rawInput.trim().split("\\s+");
                if(damageParts.length < 2){
                    throw new IllegalArgumentException("Usage: /pickDamage <card1> <card2> ...");
                }

                int count = damageParts.length -1; //Anzahl der Karten
                List<String> picked = new ArrayList<>();

                for(int i = 1; i < damageParts.length; i++){
                    picked.add(damageParts[i]);
                }
                PickDamageBody body = new PickDamageBody(count, picked);
                PickDamageMessage message = new PickDamageMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "#move" : {
                // #move <steps>
                String[] moveParts = rawInput.trim().split("\\s+");
                if (moveParts.length < 2) {
                    throw new IllegalArgumentException("Usage: #move <steps>");
                }
                int steps = Integer.parseInt(moveParts[1]);
                CheatMoveBody body = new CheatMoveBody(steps);
                CheatMoveMessage message = new CheatMoveMessage(body);
                return objectMapper.writeValueAsString(message);
            }

            case "#turn" : {
                // #turn <left|right|u>
                String[] turnParts = rawInput.trim().split("\\s+");
                if (turnParts.length < 2) {
                    throw new IllegalArgumentException("Usage: #turn <left|right|u>");
                }
                String direction = turnParts[1];
                if (!direction.equals("left") && !direction.equals("right") && !direction.equals("u")) {
                    throw new IllegalArgumentException("Direction must be left, right, or u");
                }
                CheatTurnBody body = new CheatTurnBody(direction);
                CheatTurnMessage message = new CheatTurnMessage(body);
                return objectMapper.writeValueAsString(message);
            }
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }
}

