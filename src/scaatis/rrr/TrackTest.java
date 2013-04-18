package scaatis.rrr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.Curve;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.rrr.tracktiles.Straight;

public class TrackTest extends JPanel {
	private static final long serialVersionUID = 1L;
	private Track track;
	private Ellipse2D circle;
	private boolean running;
	private int i;

	public TrackTest() {
		track = new Track(Direction.LEFT, new FinishLine(Direction.LEFT),
				new Straight(Direction.LEFT), new Curve(Direction.LEFT),
				new CheckPoint(Direction.UP),
				new Curve(Direction.UP), new Straight(Direction.RIGHT),
				new CheckPoint(Direction.RIGHT), new Curve(Direction.RIGHT),
				new Straight(Direction.DOWN),
				new Curve(Direction.DOWN));
		circle = new Ellipse2D.Double(30, 30, 30, 30);
		running = false;
		i = 0;
	}
	
	@Override
	public Dimension getPreferredSize() {
		//return track.getTrackArea().getBounds().getSize();
		return new Dimension(860, 215);
	}

	public static void main(String[] args) {
		final TrackTest test = new TrackTest();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowShit(test);
			}
		});
		test.run();
	}
	
	public static void createAndShowShit(TrackTest t) {
		JFrame frame = new JFrame("Track Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(t);
		frame.pack();
		frame.setVisible(true);
	}

	public void run() {
		running = true;
		while (running) {
			i++;
			repaint();
			try {
				Thread.sleep(1000 / 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		//g2d.transform(AffineTransform.getTranslateInstance(430, 0));
		g2d.transform(AffineTransform.getTranslateInstance(getWidth() / 2, 0));
		g2d.transform(AffineTransform.getScaleInstance(1, 0.25));
		g2d.transform(AffineTransform.getRotateInstance(Math.PI / 4));
		g2d.setColor(Color.white);
		g2d.fill(track.getTrackArea());
		g2d.setColor(Color.green);
		g2d.fill(track.getFinishLine());
		g2d.setColor(Color.yellow);
		for(Area checkPoint : track.getCheckpoints()) {
			g2d.fill(checkPoint);
		}
		
		// Draw Circle
		Point2D pos = getPosition(i);
		g2d.transform(AffineTransform.getTranslateInstance(pos.getX(), pos.getY()));
		g2d.setColor(Color.darkGray);
		g2d.fill(circle);
	}

	private Point2D getPosition(int i) {
		double angle =  -((double) i) / 50.0 * Math.PI;
		double x = Math.sin(angle) * 200;
		double y = Math.cos(angle) * 200;
		return new Point2D.Double(270 + x, 270 + y);
	}

	public void stop() {
		running = false;
	}
}
