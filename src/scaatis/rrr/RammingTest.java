package scaatis.rrr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

public class RammingTest extends DrivingTest {

	private static final long serialVersionUID = 1L;
	private Car dummy;

	public RammingTest() {
		dummy = new Car(new Point2D.Double(150, 150), Direction.UP);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.gray);
		g2d.fill(dummy.getArea());
	}

	public static void main(String[] args) {
		final DrivingTest test = new RammingTest();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowShit(test);
			}
		});
		test.run();
	}

	public void update(double delta) {
		super.update(delta);
		dummy.update(delta);
		Area intersect = dummy.getArea();
		intersect.intersect(getCar().getArea());
		if (!intersect.isEmpty()) {
			Car.collide(getCar(), dummy);
		}
		intersect = dummy.getArea();
		intersect.intersect(getTrack().getNegative());
		if (!intersect.isEmpty()) {
			dummy.collideWith(getTrack());
		}
	}
}
