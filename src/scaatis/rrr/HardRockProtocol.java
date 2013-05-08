package scaatis.rrr;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scaatis.rrr.tracktiles.TrackTile;
import scaatis.util.Util;

public class HardRockProtocol {

    public static final int                      port = 1993;
    private static final HashMap<String, Number> constants;
    private static final JSONObject              successfulHandshake;
    static {
        successfulHandshake = new JSONObject();
        successfulHandshake.put("message", "connect");
        successfulHandshake.put("status", true);
    }
    static {
        constants = new HashMap<>();
        constants.put("car.hitbox.width", Car.hitbox.getWidth());
        constants.put("car.hitbox.height", Car.hitbox.getHeight());
        constants.put("car.maxfriction", Car.frontFriction);
        constants.put("car.minfriction", Car.sidewaysFriction);
        constants.put("car.acceleration", Car.acceleration);
        constants.put("car.boostedacceleration", Car.acceleration);
        constants.put("car.topspeed", Car.topSpeed);
        constants.put("car.boostedtopspeed", Car.boostSpeed);
        constants.put("car.turnspeed", Car.turningSpeed);
        constants.put("car.minturnspeed", Car.minSpeed);
        constants.put("car.collision.rotation", Car.collisionRotation);
        constants.put("car.collision.bounce", Car.collisionRepulsion);
        constants.put("car.collision.mindamagespeed", Car.damageThreshHold);
        constants.put("car.hp", Car.maxHP);
        constants.put("missile.hitbox.width", Missile.hitbox.getWidth());
        constants.put("missile.hitbox.height", Missile.hitbox.getHeight());
        constants.put("missile.speed", Missile.speed);
        constants.put("missile.range", Missile.range);
        constants.put("missile.damage", Missile.damage);
        constants.put("mine.hitbox.width", Mine.hitbox.getWidth());
        constants.put("mine.hitbox.height", Mine.hitbox.getHeight());
        constants.put("mine.damage", Mine.damage);
        constants.put("mine.stuntime", Mine.disableDuration);
        constants.put("player.maxmissiles", Player.maxMissiles);
        constants.put("player.maxboosts", Player.maxBoosts);
        constants.put("player.maxmines", Player.maxMines);
        constants.put("race.laps", HardRockRacing.laps);
        constants.put("track.straight.legnth", TrackTile.SEGMENT_LENGTH);
        constants.put("track.straight.width", TrackTile.TRACK_WIDTH);
        constants.put("track.curve.innerradius", TrackTile.SEGMENT_LENGTH);
        constants.put("track.curve.outerradius", TrackTile.SEGMENT_LENGTH + TrackTile.TRACK_WIDTH);
    }

    public static void log(Object source, String message) {
        System.out.println("[" + source.getClass().getSimpleName() + "] " + message);
    }

    private ConcurrentLinkedQueue<Connection>     disconnected;
    private HardRockRacing                        game;
    private boolean                               hasPlayers;
    private ConnectionListener                    listener;
    private ConcurrentHashMap<Connection, Player> players;
    private ConcurrentLinkedQueue<Player>         playerQueue;
    private ServerSocket                          server;

    public HardRockProtocol(HardRockRacing game) {
        this.game = game;
        players = new ConcurrentHashMap<>();
        disconnected = new ConcurrentLinkedQueue<>();
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log(this, "Opened socket on port " + server.getLocalPort());
        listener = new ConnectionListener(server, this);
        hasPlayers = false;
        playerQueue = new ConcurrentLinkedQueue<>();
    }

    public void connectPlayer(Connection connection, Player player) {
        // check name
        if (!player.isObserver()) {
            for (Player player2 : players.values()) {
                if (player2.isObserver()) {
                    continue;
                }
                if (player2.getName().equals(player.getName())) {
                    connection.send("Could not connect, name already in use.");
                    connection.close();
                    return;
                }
            }
            playerQueue.add(player);
        }
        players.put(connection, player);
        if (player.isObserver() && game.getCurrentTrack() != null) {
            connection.send(getGameStartMessage(player.transferMapTiled()));
        }
        log(this, "Player " + player.toString() + " connected successfully.");
        hasPlayers = getNumberOfPlayers() > 0;
        game.updateGUI();
    }

    public Set<Map.Entry<Connection, Player>> getConnectionEntries() {
        return players.entrySet();
    }

    public int getNumberOfConnections() {
        return players.size();
    }

    public int getNumberOfObservers() {
        return getNumberOfConnections() - getNumberOfPlayers();
    }

    public int getNumberOfPlayers() {
        int i = 0;
        for (Player player : players.values()) {
            if (!player.isObserver()) {
                i++;
            }
        }
        return i;
    }

    public void sendDestroyed(Car car) {
        JSONObject message = new JSONObject();
        message.put("message", "destroyed");
        message.put("car", car.getID());
        sendToAll(message.toString());
    }

    public void sendGameStart() {
        String notTiled = getGameStartMessage(false).toString();
        String tiled = getGameStartMessage(true).toString();
        for (Connection connection : this.players.keySet()) {
            Player player = this.players.get(connection);
            if (player == null) {
                continue;
            }
            if (!player.isObserver() && !game.getRacers().contains(player)) {
                continue;
            }
            if (player.transferMapTiled()) {
                connection.send(tiled);
            } else {
                connection.send(notTiled);
            }
        }
    }

    public void sendGameState() {
        JSONObject gamestate = new JSONObject();
        gamestate.put("message", "gamestate");
        gamestate.put("time", game.getTimer());
        ArrayList<JSONObject> cars = new ArrayList<>();
        for (Player player : game.getRacers()) {
            Car car = player.getCar();
            if (car == null) {
                continue;
            }
            JSONObject obj = car.toJSON();
            obj.put("driver", player.getName());
            obj.put("missiles", player.getMissiles());
            obj.put("boosts", player.getBoosts());
            obj.put("mines", player.getMines());
            obj.put("lapscomplete", player.getCompletedLaps());
            cars.add(obj);
        }
        gamestate.put("cars", cars);

        ArrayList<JSONObject> missiles = new ArrayList<>();
        for (Missile missile : game.getMissiles()) {
            missiles.add(missile.toJSON());
        }
        gamestate.put("missiles", missiles);

        ArrayList<JSONObject> mines = new ArrayList<>();
        for (Mine mine : game.getMines()) {
            mines.add(mine.toJSON());
        }
        gamestate.put("mines", mines);

        sendToAll(gamestate.toString());
    }

    public void sendLapComplete(Player player) {
        JSONObject message = new JSONObject();
        message.put("message", "lapcomplete");
        message.put("player", player.getName());
        message.put("lapsleft", HardRockRacing.laps - player.getCompletedLaps());
        sendToAll(message.toString());
    }

    public void sendMineHit(Mine mine, Player victim) {
        JSONObject message = new JSONObject();
        message.put("message", "minehit");
        message.put("mine", mine.getID());
        message.put("target", victim.getName());
        sendToAll(message.toString());
    }

    public void sendMissileHit(Missile missile, Player victim) {
        JSONObject message = new JSONObject();
        message.put("message", "missilehit");
        message.put("missile", missile.getID());
        message.put("target", victim.getName());
        message.put("shooter", missile.getShooter().getName());
        sendToAll(message.toString());
    }
    
    public void sendBoost(Player player) {
        JSONObject message = new JSONObject();
        message.put("message", "boost");
        message.put("player", player.getName());
        sendToAll(message.toString());
    }

    public void sendRaceOver(List<Player> placement) {
        JSONObject message = new JSONObject();
        message.put("message", "raceover");
        JSONArray array = new JSONArray();
        for (Player player : placement) {
            array.put(player.toJSON());
        }
        message.put("placement", array);
        sendToAll(message.toString());
        for (Player player : placement) {
            playerQueue.add(player);
        }
    }

    public void sendToAll(String message) {
        for (Connection connection : players.keySet()) {
            Player p = players.get(connection);
            if (p == null) {
                continue;
            }
            if (game.getState() != RaceState.RACE
                    || p.isObserver()
                    || game.getRacers().contains(p)) {
                connection.send(message);
            }
        }
    }

    public void start() {
        new Thread(listener).start();
    }

    public void stop() {
        listener.stop();
        for (Connection connection : players.keySet()) {
            connection.close();
        }
        try {
            server.close();
        } catch (IOException e) {
        }
        log(this, "Server was stopped.");
    }

    public void update() {
        while (!disconnected.isEmpty()) {
            disconnect(disconnected.poll());
        }

        for (Connection connection : players.keySet()) {
            while (connection.hasInput()) {
                parseInput(connection, connection.nextInput());
            }
        }
        if (game.getState() == RaceState.WAITING && hasPlayers) {
            startRace();
        }
    }

    protected Player attemptHandShake(String line, PrintWriter err) {
        JSONObject object;
        try {
            object = new JSONObject(line);
        } catch (JSONException e) {
            err.println(e.getMessage());
            return null;
        }

        String message = getString(object, "message");
        if (message == null) {
            err.println("no message field");
            return null;
        }
        if (!message.equals("connect")) {
            err.println("Expected handshake, got " + message);
            return null;
        }
        String type = getString(object, "type");
        if (type == null) {
            err.println("no type field");
            return null;
        }
        if (type.equals("observer")) {
            boolean tiledMap = false;
            try {
                tiledMap = object.getBoolean("tracktiled");
            } catch (JSONException e) {

            }
            log(this, "Handshake successful for new observer with tiledTrack " + tiledMap);
            err.println(successfulHandshake.toString());
            return new Player(tiledMap);
        } else if (type.equals("player")) {
            String name = getString(object, "name");
            if (name == null || name.equals("")) {
                err.println("No name field");
                return null;
            }

            String character = getString(object, "character");
            if (character == null) {
                return null;
            }
            RaceCharacter charac = RaceCharacter.getFromName(character);
            if (charac == null) {
                err.println("Unknown character: " + character);
                return null;
            }

            String ctype = getString(object, "cartype");
            if (ctype == null) {
                err.println("No cartype field");
                return null;
            }
            CarType carType = CarType.getFromString(ctype);
            if (carType == null) {
                err.println("Unknown car type: " + ctype);
                return null;
            }

            boolean tiledMap = false;
            try {
                tiledMap = object.getBoolean("tracktiled");
            } catch (JSONException e) {

            }
            Player player = new Player(name, charac, carType, tiledMap);
            HardRockProtocol.log(this, "Handshake successful for new Player: " + player.toString());
            err.println(successfulHandshake.toString());
            return player;
        } else {
            err.println("Expected one of \"player\" or \"observer\" for type.");
            return null;
        }
    }

    protected void connectionLost(Connection connection) {
        disconnected.add(connection);
    }

    protected void parseInput(Connection connection, String message) {
        JSONObject object;
        try {
            object = new JSONObject(message);
        } catch (JSONException e) {
            connection.send(e.getMessage());
            return;
        }
        String msg;
        try {
            msg = object.getString("message");
        } catch (JSONException e) {
            connection.send(e.getMessage());
            return;
        }
        if (msg == null) {
            connection.send("Invalid message: " + null);
            return;
        }
        if (msg.equals("action")) {
            parseAction(connection, object);
            return;
        } else if (msg.equals("request")) {
            parseRequest(connection, object);
        } else {
            connection.send("Unknown message: " + message);
        }
    }

    protected void startRace() {
        // Pick racers
        ArrayList<Player> racers = new ArrayList<>();

        for (int i = 0; i < 4 && !playerQueue.isEmpty(); i++) {
            racers.add(playerQueue.poll());
        }

        // Assign Characters
        List<RaceCharacter> characters = new ArrayList<>(Arrays.asList(RaceCharacter.values()));
        for (Player player : racers) {
            if (characters.contains(player.getPreferredCharacter())) {
                player.setCharacter(player.getPreferredCharacter());
                characters.remove(player.getPreferredCharacter());
            } else {
                RaceCharacter character = Util.getRandom(characters);
                player.setCharacter(character);
                characters.remove(character);
            }
        }
        game.startPreRace(racers);
    }

    private JSONObject getGameStartMessage(boolean tiled) {
        JSONObject message = new JSONObject();
        message.put("message", "gamestart");

        ArrayList<JSONObject> players = new ArrayList<>();
        for (Player player : game.getRacers()) {
            players.add(player.toJSON());
        }
        message.put("players", players);

        message.put("laps", HardRockRacing.laps);

        message.put("track", game.getCurrentTrack().toJSON(tiled));
        return message;
    }

    private void disconnect(Connection connection) {
        Player player = players.get(connection);
        if (player != null) {
            log(this, "Player " + player.toString() + " disconnected.");
            game.dropPlayer(player);
        }
        players.remove(connection);
        playerQueue.remove(player);
        hasPlayers = getNumberOfPlayers() > 0;
        game.updateGUI();
    }

    private String getString(JSONObject obj, String key) {
        String val = null;
        try {
            val = obj.getString(key);
        } catch (JSONException e) {
        }
        return val;
    }

    private void parseAction(Connection connection, JSONObject action) {
        Player player = players.get(connection);
        if (player == null || !game.getRacers().contains(player)) {
            return;
        }
        String type;
        try {
            type = action.getString("type");
        } catch (JSONException e) {
            connection.send(e.getMessage());
            return;
        }
        if (type == null) {
            connection.send("Invalid action type: " + null);
            return;
        }
        if (type.equals("turnleft")) {
            player.turnLeft();
        } else if (type.equals("turnright")) {
            player.turnRight();
        } else if (type.equals("accelerate")) {
            player.accelerate();
        } else if (type.equals("stopaccelerate")) {
            player.stopAccelerating();
        } else if (type.equals("stopturning")) {
            player.stopTurning();
        } else if (type.equals("missile")) {
            player.fireMissile();
        } else if (type.equals("boost")) {
            player.boost();
            sendBoost(player);
        } else if (type.equals("mine")) {
            player.dropMine();
        } else {
            connection.send("Unknown action type: " + type);
            return;
        }
        action.put("player", player.getName());
        sendToAll(action.toString());
    }

    private void parseRequest(Connection connection, JSONObject request) {
        String key;
        try {
            key = request.getString("key");
        } catch (JSONException e) {
            connection.send(e.getMessage());
            return;
        }
        Number value = constants.get(key);
        JSONObject response = new JSONObject();
        response.put("message", "constant");
        response.put("name", key);
        if (value == null) {
            response.put("value", JSONObject.NULL);
        } else {
            response.put("value", value.toString());
        }
        connection.send(response);
    }
}
