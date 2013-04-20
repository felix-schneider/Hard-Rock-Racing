package scaatis.rrr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

public class ShootyTest extends RammingTest {

	private static final long serialVersionUID = 1L;
	private List<Missile> missiles;

	public ShootyTest() {
		missiles = new ArrayList<>();
	}

	public static void main(String[] args) {
		final DrivingTest test = new ShootyTest();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowShit(test);
			}
		});
		test.run();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.red);
		for (Missile m : missiles) {
			g2d.fill(m.getArea());
		}
	}

	public void update(double delta) {
		super.update(delta);
		for (Missile m : missiles) {
			m.update(delta);
			Area a = m.getArea();
			a.intersect(getDummy().getArea());
			if(!a.isEmpty()) {
				m.collideWith(getDummy());
			}
		}
		Iterator<Missile> iter = missiles.iterator();
		while (iter.hasNext()) {
			if (iter.next().isDestroyed()) {
				iter.remove();
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_SPACE ) {
			missiles.add(getCar().fireMissile());
			getCar().setMissiles(3);
		}
	}
}
