package scaatis.rrr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
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

	// private double diagonal;

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
		Area finish = track.getFinishLine().getArea();
		Point start = new Point(finish.getBounds().x + 90,
				finish.getBounds().y + 113);
		car = new Car(start, track.getStartDirection());
		running = false;
		// double w = track.getTrackArea().getBounds2D().getWidth();
		// double h = track.getTrackArea().getBounds2D().getHeight();
		// diagonal = Math.sqrt(w * w + h * h);
	}

	@Override
	public Dimension getPreferredSize() {
		// return new Dimension((int) diagonal, (int) (diagonal / 4));
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
		long timeA = System.nanoTime();
		long timeB = timeA;
		while (running) {
			try {
				Thread.sleep((int) (1000 / 30.0 - (timeB - timeA) / 1e6));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeB = System.nanoTime();
			double delta = (timeB - timeA) / 1e9;
			timeA = System.nanoTime();
			update(delta);
			repaint();
		}
	}

	protected void update(double delta) {
		car.update(delta);
		Area intersection = car.getArea();
		intersection.intersect(track.getNegative());
		if (!intersection.isEmpty()) {
			car.collideWith(track);
		}
	}

	public void stop() {
		running = false;
	}

	public static void createAndShowShit(DrivingTest t) {
		JFrame frame = new JFrame(t.getClass().getSimpleName());
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
		// I don't wanna do math! you can't make me!
		// g2d.transform(AffineTransform
		// .getTranslateInstance(diagonal / 2 - 50, 0));
		// g2d.transform(AffineTransform.getScaleInstance(1, .25));
		// g2d.transform(AffineTransform.getRotateInstance(.25 * Math.PI));
		g2d.scale(.5, .5);
		g2d.setColor(Color.white);
		g2d.fill(track.getTrackArea());
		g2d.setColor(Color.gray);
		g2d.fill(car.getArea());
	}

	private boolean up = false;
	private boolean right = false;
	private boolean left = false;
	private boolean down = false;

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP && !up) {
			car.setAccelerating(1);
			up = true;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN && !down) {
			car.setAccelerating(-1);
			down = true;
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
			if (down) {
				car.setAccelerating(-1);
			} else {
				car.setAccelerating(0);
			}
			up = false;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (up) {
				car.setAccelerating(1);
			} else {
				car.setAccelerating(0);
			}
			down = false;
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

	protected Track getTrack() {
		return track;
	}

	protected Car getCar() {
		return car;
	}
}
