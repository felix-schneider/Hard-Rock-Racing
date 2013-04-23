package scaatis.rrr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scaatis.rrr.event.DestroyedEvent;

public class HardRockRacing {
	public static final int fps = 30;
	public static final int pauseBeforeRace = 5;
	public static final int pauseAfterRace = 10;
	public static final int laps = 4;
	public static final boolean debug = true;

	private Track currentTrack;
	private int curIndex;
	private List<String> maps;
	private boolean loop;
	private double pause;
	private HardRockProtocol protocol;
	private RaceState state;
	private List<Player> racers;

	private boolean running;
	
	private HardRockRacing() {
		running = false;
		pause = 0;
		this.maps = null;
		this.loop = false;
		curIndex = 0;
		currentTrack = null;
		protocol = new HardRockProtocol(this);
		state = RaceState.WAITING;
	}
	
	public HardRockRacing(List<String> maps, boolean loop) {
		running = false;
		pause = 0;
		this.maps = maps;
		this.loop = loop;
		curIndex = 0;
		currentTrack = null;
		if (maps.size() == 0) {
			throw new IllegalArgumentException("No map rotation!");
		}
		protocol = new HardRockProtocol(this);
		state = RaceState.WAITING;
	}

	public void startNewRace() {
		state = RaceState.PRERACE;
		try {
			currentTrack = TrackLoader.loadTrack(maps.get(0));
		} catch (IOException e) {
			protocol.stop();
			throw new RuntimeException(e);
		}
		pause = pauseBeforeRace;
		protocol.mapChange();
	}

//	public void objectDestroyed(DestroyedEvent e) {
//		if (e.getSource() instanceof GameObject) {
//			toBeDestroyed.add((GameObject) e.getSource());
//		}
//	}

	public void start() {
		running = true;
		long timeA = System.nanoTime();
		long timeB = timeA;
		while (running) {
			try {
				Thread.sleep((int) (1000 / ((double) fps) - (timeB - timeA) / 1e6));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			protocol.processInput();
			timeB = System.nanoTime();
			double delta = (timeB - timeA) / 1e9;
			timeA = System.nanoTime();
			update(delta);
			timeB = System.nanoTime();
		}
		protocol.stop();
	}

	private void update(double delta) {
//		if (pause > 0) {
//			pause -= delta;
//			return;
//		} else if (state == RaceState.WAITING) {
//			return;
//		}
//		for (GameObject obj : objects) {
//			obj.update(delta);
//		}
//		collisions();
	}

	private void collisions() {
//		for (Missile missile : missiles) {
//			for (Player player : racers) {
//
//			}
//		}
	}
	
	public List<Player> getRacers() {
		return racers;
	}
	
	public Track getCurrentTrack() {
		return currentTrack;
	}
	
	public static void main(String[] args) {
		HardRockRacing racing = new HardRockRacing();
		racing.start();
	}
}
