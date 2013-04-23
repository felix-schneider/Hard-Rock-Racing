package scaatis.rrr;

import java.io.IOException;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

public class Handshake implements Runnable {
	public static final int handShakeTimeOut = 5;
	private Connection connection;
	private HardRockProtocol protocol;
	private static final JSONObject handshakeSuccess = new JSONObject();
	static {
		handshakeSuccess.put("message", "connect");
		handshakeSuccess.put("status", true);
	}
	

	public Handshake(Connection connection, HardRockProtocol protocol) {
		this.connection = connection;
		this.protocol = protocol;
	}

	public void run() {
		HardRockProtocol.log(this, "Waiting for handshake...");
		String line = null;
		try {
			System.out.println(connection.getSocket().getSoTimeout());
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		try {
			line = connection.getLine();
		} catch (IOException e) {
			try {
				connection.close();
			} catch (IOException e2) {
				HardRockProtocol.log(this, "Closing connection failed.");
			}
			HardRockProtocol.log(this, "Unexpected disconnect.");
			return;
		}
		if (line == null) {
			HardRockProtocol.log(this, connection.getSocket().getInetAddress()
					.toString()
					+ " did not send handshake.");
			return;
		}

		boolean success = attemptHandShake(line);
		if (!success) {
			HardRockProtocol.log(this, "Handshake from "
					+ connection.getSocket().getInetAddress().toString()
					+ " was unsuccessful.");
		} else {
			connection.println(handshakeSuccess.toString());
		}
	}

	private boolean attemptHandShake(String line) {
		JSONObject object;
		try {
			object = new JSONObject(line);
		} catch (JSONException e) {
			connection.println(e.getMessage());
			return false;
		}

		String message = getValue(object, "message");
		if (message == null) {
			System.out.println("hi");
			return false;
		}
		if (!message.equals("connect")) {
			connection.println("Expected handshake, got " + message);
			return false;
		}
		String type = getValue(object, "type");
		if (type == null) {
			return false;
		}
		if (type.equals("observer")) {
			protocol.connectPlayer(connection, new Player());
			HardRockProtocol.log(this, "Handshake successful for new observer");
			return true;
		} else if (type.equals("player")) {
			String name = getValue(object, "name");
			if (name == null) {
				return false;
			}
			String character = getValue(object, "character");
			if (character == null) {
				return false;
			}
			RaceCharacter charac = RaceCharacter.getFromName(character);
			if (charac == null) {
				connection.println("Unknown character: " + character);
				return false;
			}
			String ctype = getValue(object, "cartype");
			if (ctype == null) {
				return false;
			}
			CarType carType = CarType.getFromString(ctype);
			if (carType == null) {
				connection.println("Unknown car type: " + ctype);
				return false;
			}
			boolean tiledMap = false;
			try {
				tiledMap = object.getBoolean("tracktiled");
			} catch (JSONException e) {

			}
			Player player = new Player(name, charac, carType, tiledMap);
			protocol.connectPlayer(connection, player);
			HardRockProtocol.log(this, "Handshake successful for new Player: "
					+ player.toString());
			return true;
		} else {
			connection
					.println("Expected one of \"player\" or \"observer\" for type.");
			return false;
		}
	}

	private String getValue(JSONObject obj, String key) {
		String val = null;
		try {
			val = obj.getString(key);
		} catch (JSONException e) {
			connection.println(e.getMessage());
		}
		return val;
	}
}
