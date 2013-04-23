package scaatis.rrr;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HardRockProtocol {
	private ConcurrentHashMap<Connection, Player> players;
	private ServerSocket server;
	private Thread listener;
	private HardRockRacing game;
	private long timeA;
	private long timeB;

	public HardRockProtocol(HardRockRacing game) {
		this.game = game;
		players = new ConcurrentHashMap<>();
		try {
			server = new ServerSocket(1993);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log(this, "Opened socket on port " + server.getLocalPort());
		listener = new Thread(new ConnectionListener(server, this));
		listener.start();
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
		// TODO Auto-generated method stub

	}

	public void mapChange() {
		// TODO Auto-generated method stub

	}

	public void processInput() {
		// TODO Auto-generated method stub
		timeB = System.nanoTime();
		if (timeB - timeA > 1e9) {
			timeA = timeB;
			log(this, players.toString());
		}
	}

	public void connectPlayer(Connection connection, Player player) {
		players.put(connection, player);
		if (game.getCurrentTrack() != null) {
			transmitTrack(connection);
		}
		log(this, "Player " + player.getName()
				+ " connected successfully.");
	}

	public void transmitTrack(Connection connection) {

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
}
