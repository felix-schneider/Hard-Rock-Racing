package scaatis.rrr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.Curve;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.rrr.tracktiles.Straight;
import scaatis.rrr.tracktiles.TrackTile;

public class DrivingTest extends JPanel implements KeyListener {

	private static final long serialVersionUID = 1291372903573772594L;
	private Track track;
	private Car car;
	private boolean running;

	public DrivingTest() {
		ArrayList<TrackTile> tiles = new ArrayList<>();
		tiles.add(new FinishLine(Direction.LEFT));
		for (int i = 0; i < 10; i++) {
			tiles.add(new Straight(Direction.LEFT));
		}
		tiles.add(new Curve(Direction.LEFT));
		for (int i = 0; i < 8; i++) {
			tiles.add(new Straight(Direction.UP));
		}
		tiles.add(new Curve(Direction.UP));
		for (int i = 0; i < 10; i++) {
			tiles.add(new Straight(Direction.RIGHT));
		}
		tiles.add(new CheckPoint(Direction.RIGHT));
		tiles.add(new Curve(Direction.RIGHT));
		for (int i = 0; i < 8; i++) {
			tiles.add(new Straight(Direction.DOWN));
		}
		tiles.add(new Curve(Direction.DOWN));
		track = new Track(Direction.LEFT, tiles);
		Point start = new Point(track.getFinishLine().getBounds().x + 90, track
				.getFinishLine().getBounds().y + 113);
		car = new Car(start, track.getStartDirection());
		running = false;
	}

	@Override
	public Dimension getPreferredSize() {
		return track.getTrackArea().getBounds().getSize();
	}

	public static void main(String[] args) {
		final DrivingTest test = new DrivingTest();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowShit(test);
			}
		});
		test.run();
	}

	public void run() {
		running = true;
		while (running) {
			long timeA = System.nanoTime();
			try {
				Thread.sleep(1000 / 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long timeB = System.nanoTime();
			double delta = (timeB - timeA) / 1e9;
			car.update(delta);
			repaint();
		}
	}

	public void stop() {
		running = false;
	}

	public static void createAndShowShit(DrivingTest t) {
		JFrame frame = new JFrame("Driving Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(t);
		frame.pack();
		frame.addKeyListener(t);
		frame.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.white);
		g2d.fill(track.getTrackArea());
		g2d.setColor(Color.darkGray);
		g2d.fill(car.getArea());
	}

	private boolean up = false;
	private boolean right = false;
	private boolean left = false;

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP && !up) {
			car.setAccelerating(true);
			System.out.println("forward!");
			up = true;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT && !left) {
			car.setTurning(Direction.LEFT);
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT && !right) {
			car.setTurning(Direction.RIGHT);
			right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			stop();
			System.exit(0);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			car.setAccelerating(false);
			up = false;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			if (right) {
				car.setTurning(Direction.RIGHT);
			} else {
				car.stopTurning();
			}
			left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if (left) {
				car.setTurning(Direction.LEFT);
			} else {
				car.stopTurning();
			}
			right = false;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
