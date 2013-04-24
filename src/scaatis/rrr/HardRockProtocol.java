package scaatis.rrr;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import scaatis.rrr.tracktiles.TrackTile;

public class HardRockProtocol {
	public static final int port = 1993;
	private ConcurrentHashMap<Connection, Player> players;
	private ServerSocket server;
	private ConnectionListener listener;
	private HardRockRacing game;
	private long timeA;
	private long timeB;
	private static final JSONObject successfulHandshake;
	static {
		successfulHandshake = new JSONObject();
		successfulHandshake.put("message", "connect");
		successfulHandshake.put("status", true);
	}
	private static final HashMap<String, Number> constants;
	static {
		constants = new HashMap<>();
		constants.put("car.hitbox.width", Car.hitbox.getWidth());
		constants.put("car.hitbox.height", Car.hitbox.getHeight());
		constants.put("car.maxfriction", Car.frontFriction);
		constants.put("car.minfriction", Car.sidewaysFriction);
		constants.put("car.acceleration", Car.acceleration);
		constants.put("car.topspeed", Car.topSpeed);
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
		constants.put("player.maxmissiles", Player.maxMissiles);
		constants.put("race.laps", HardRockRacing.laps);
		constants.put("track.straight.legnth", TrackTile.SEGMENT_LENGTH);
		constants.put("track.straight.width", TrackTile.TRACK_WIDTH);
		constants.put("track.curve.innerradius", TrackTile.SEGMENT_LENGTH);
		constants.put("track.curve.outerradius", TrackTile.SEGMENT_LENGTH
				+ TrackTile.TRACK_WIDTH);
	}

	public HardRockProtocol(HardRockRacing game) {
		this.game = game;
		players = new ConcurrentHashMap<>();
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log(this, "Opened socket on port " + server.getLocalPort());
		listener = new ConnectionListener(server, this);
		new Thread(listener).start();
		timeA = System.nanoTime();
	}

	public int getNumberOfConnections() {
		return players.size();
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

	public int getNumberOfObservers() {
		return getNumberOfConnections() - getNumberOfPlayers();
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
	}

	public void mapChange() {
		for (Connection connection : players.keySet()) {
			transmitTrack(connection);
		}
	}

	public void processInput() {
		for (Connection connection : players.keySet()) {
			while (connection.hasInput()) {
				parseInput(connection, connection.nextInput());
			}
		}

		// DEBUG
		timeB = System.nanoTime();
		if (timeB - timeA > 1e9) {
			timeA = timeB;
			log(this, players.toString());
		}
	}

	public void sendGameState() {
		JSONObject gamestate = new JSONObject();
		gamestate.put("message", "gamestate");
		gamestate.put("time", game.getTimer());
		ArrayList<JSONObject> cars = new ArrayList<>();
		for (Player player : game.getRacers()) {
			Car car = player.getCar();
			JSONObject obj = car.toJSON();
			obj.put("message", "car");
			obj.put("id", car.getID());
			obj.put("driver", player.getName());
			obj.put("hp", car.getHP());
			obj.put("facing", car.getFacing());
			obj.put("missiles", player.getMissiles());
			obj.put("lapscomplete", player.getCompletedLaps());
			obj.put("accelerating", car.getAccelerating() == 1);
			obj.put("turning", car.getTurning());
			cars.add(obj);
		}
		gamestate.put("cars", cars);
		ArrayList<JSONObject> missiles = new ArrayList<>();
		for (Missile missile : game.getMissiles()) {
			missiles.add(missile.toJSON());
		}
		gamestate.put("missiles", missiles);
		sendToAll(gamestate.toString());
	}

	public void sendLapComplete(Player player) {
		JSONObject message = new JSONObject();
		message.put("message", "lapcomplete");
		message.put("player", player.getName());
		message.put("lapsleft", HardRockRacing.laps - player.getCompletedLaps());
		sendToAll(message.toString());
	}

	public void sendDestroyed(Player player) {
		JSONObject message = new JSONObject();
		message.put("message", "destroyed");
		message.put("car", player.getCar().getID());
		sendToAll(message.toString());
	}

	public void sendMissileHit(Missile missile, Player victim) {
		JSONObject message = new JSONObject();
		message.put("message", "missilehit");
		message.put("target", victim.getName());
		message.put("shooter", missile.getShooter().getName());
		sendToAll(message.toString());
	}

	public void sendRaceOver(List<Player> placement) {
		JSONObject message = new JSONObject();
		message.put("message", "raceover");
		message.put("placement", placement);
		sendToAll(message.toString());
	}

	public void sendRaceAborted() {
		JSONObject message = new JSONObject();
		message.put("message", "raceaborted");
		sendToAll(message.toString());
	}

	public void connectPlayer(Connection connection, Player player) {
		// check name
		for (Player player2 : players.values()) {
			if (player2.getName().equals(player.getName())) {
				connection.send("Could not connect, name already in use.");
				connection.close();
				return;
			}
		}
		players.put(connection, player);
		if (game.getCurrentTrack() != null) {
			transmitTrack(connection);
		}
		log(this, "Player " + player.getName() + " connected successfully.");
	}

	public void transmitTrack(Connection connection) {
		if (game.getCurrentTrack() == null) {
			return;
		}
		Player pl = players.get(connection);
		if (pl == null) {
			return;
		}
		connection.send(game.getCurrentTrack().toJSON(pl.transferMapTiled()));
	}

	public void sendToAll(String message) {
		for (Connection connection : players.keySet()) {
			Player p = players.get(connection);
			if (p == null) {
				continue;
			}
			if (game.getState() != RaceState.RACE || p.isObserver()
					|| game.getRacers().contains(p)) {
				connection.send(message);
			}
		}
	}

	public void connectionLost(Connection connection) {
		Player player = players.get(connection);
		if (player != null) {
			System.out.println("Player " + player.getName() + " disconnected.");
			game.dropPlayer(player);
		}
		players.remove(connection);
	}

	public static void log(Object source, String message) {
		System.out.println("[" + source.getClass().getSimpleName() + "] "
				+ message);
	}

	public List<RaceCharacter> getAvailableCharacters() {
		List<RaceCharacter> pool = Arrays.asList(RaceCharacter.values());
		for (Player player : game.getRacers()) {
			pool.remove(player.getCharacter());
		}
		return pool;
	}

	public Player attemptHandShake(String line, PrintWriter err) {
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
			log(this, "Handshake successful for new observer");
			err.println(successfulHandshake.toString());
			return new Player();
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
			System.out.println(tiledMap);
			Player player = new Player(name, charac, carType, tiledMap);
			HardRockProtocol.log(this, "Handshake successful for new Player: "
					+ player.toString());
			err.println(successfulHandshake.toString());
			return player;
		} else {
			err.println("Expected one of \"player\" or \"observer\" for type.");
			return null;
		}
	}

	private void parseInput(Connection connection, String message) {
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
		} else if (type.equals("firemissile")) {
			player.fireMissile();
		} else {
			connection.send("Unknown action type: " + type);
			return;
		}
		action.put("player", player.getName());
		sendToAll(action.toString());
	}

	private String getString(JSONObject obj, String key) {
		String val = null;
		try {
			val = obj.getString(key);
		} catch (JSONException e) {
		}
		return val;
	}
}
