package scaatis.rrr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.Curve;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.rrr.tracktiles.Straight;

public class CollisionTest extends JPanel {

	private static final long serialVersionUID = 1291372903573772594L;
	private static final int pause = 3000;
	private static final int num = 1000;
	private Track track;
	private Rectangle2D rect;
	private Area collArea;
	private Area negative;
	private int test;

	public CollisionTest() {
		track = new Track(Direction.LEFT, new FinishLine(Direction.LEFT),
				new Straight(Direction.LEFT), new Curve(Direction.LEFT),
				new CheckPoint(Direction.UP), new Straight(Direction.UP),
				new Curve(Direction.UP), new Straight(Direction.RIGHT),
				new CheckPoint(Direction.RIGHT), new Curve(Direction.RIGHT),
				new Straight(Direction.DOWN), new Straight(Direction.DOWN),
				new Curve(Direction.DOWN));
		negative = new Area(track.getTrackArea().getBounds2D());
		negative.subtract(track.getTrackArea());
		rect = new Rectangle2D.Double(0, 0, 20, 20);
		collArea = new Area(new Ellipse2D.Double(0, 0, 200, 100));
		collArea.add(new Area(new Rectangle2D.Double(130.456532, 36.79492,
				110.3456, 60.3526)));
		test = 0;
	}

	@Override
	public Dimension getPreferredSize() {
		return track.getTrackArea().getBounds().getSize();
	}

	public void test1() {
		test = 1;
		boolean coll = false;
		repaint();
		long timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(rect);
			a.intersect(negative);
			coll = !a.isEmpty();
		}
		long timeB = System.nanoTime();
		System.out
				.println(num + " collisions of Rectangle and Track which came back "
						+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Area b = new Area(rect);
		b.transform(AffineTransform.getTranslateInstance(267, 200));
		rect = b.getBounds();
		repaint();
		timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(b);
			a.intersect(negative);
			coll = !a.isEmpty();
		}
		timeB = System.nanoTime();
		System.out
				.println(num + " collisions of Rectangle and Track which came back "
						+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void test2() {
		test = 2;
		boolean coll = false;
		repaint();
		long timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(collArea);
			a.intersect(negative);
			coll = !a.isEmpty();
		}
		long timeB = System.nanoTime();
		System.out.println(num + " collisions of Shape and Track which came back "
				+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Area b = new Area(collArea);
		collArea.transform(AffineTransform.getTranslateInstance(100, 30));
		repaint();
		timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(collArea);
			a.intersect(negative);
			coll = !a.isEmpty();
		}
		timeB = System.nanoTime();
		System.out.println(num + " collisions of Shape and Track which came back "
				+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		collArea = b;
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void test3() {
		test = 3;
		boolean coll = false;
		repaint();
		long timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(collArea.getBounds2D());
			a.intersect(negative);
			coll = !a.isEmpty();
			if (coll) {
				a = new Area(collArea);
				a.intersect(negative);
				coll = !a.isEmpty();
			}
		}
		long timeB = System.nanoTime();
		System.out
				.println(num + " collisions of Shape by bounds and Track which came back "
						+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		collArea.transform(AffineTransform.getTranslateInstance(100, 30));
		repaint();
		timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(collArea.getBounds2D());
			a.intersect(negative);
			coll = !a.isEmpty();
			if (coll) {
				a = new Area(collArea);
				a.intersect(negative);
				coll = !a.isEmpty();
			}
		}
		timeB = System.nanoTime();
		System.out.println(num + " collisions of Shape by bounds and Track which came back "
				+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		collArea.transform(AffineTransform.getTranslateInstance(50, 30));
		repaint();
		timeA = System.nanoTime();
		for (int i = 0; i < num; i++) {
			Area a = new Area(collArea.getBounds2D());
			a.intersect(negative);
			coll = !a.isEmpty();
			if (coll) {
				a = new Area(collArea);
				a.intersect(negative);
				coll = !a.isEmpty();
			}
		}
		timeB = System.nanoTime();
		System.out.println(num + " collisions of Shape by bounds and Track which came back "
				+ coll + " took " + (timeB - timeA) / 10e6 + "ms");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final CollisionTest test = new CollisionTest();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowShit(test);
			}
		});
		test.test1();
		test.test2();
		test.test3();
	}

	public static void createAndShowShit(CollisionTest t) {
		JFrame frame = new JFrame("Track Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(t);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.white);
		g2d.fill(track.getTrackArea());
		if (test == 1) {
			g2d.setColor(Color.darkGray);
			g2d.fill(rect);
		} else if (test == 2) {
			g2d.setColor(Color.darkGray);
			g2d.fill(collArea);
		} else {
			g2d.setColor(Color.blue);
			g2d.fill(collArea.getBounds2D());
			g2d.setColor(Color.DARK_GRAY);
			g2d.fill(collArea);
		}
	}
}
