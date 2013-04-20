package scaatis.rrr;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import scaatis.util.Vector2D;

public class Missile extends GameObject implements Collides {
	public static final int range = 1000;
	public static final double speed = 500;
	public static final int damage = 2;
	public static final Rectangle hitbox = new Rectangle(20, 8);

	private Car shooter;
	private Point2D startPoint;

	public Missile(Car shooter) {
		super(shooter.getLocation(), new Vector2D.Polar(shooter.getFacing(),
				speed));
		this.shooter = shooter;
		startPoint = shooter.getLocation();
	}

	@Override
	public void update(double delta) {
		super.update(delta);
		if (startPoint.distanceSq(getLocation()) > range * range) {
			destroy();
		}
	}

	public void collideWith(Car other) {
		if (other != shooter) {
			other.damage(damage);
			destroy();
		}
	}

	@Override
	public Area getArea() {
		Area res = new Area(hitbox);
		res.transform(AffineTransform.getTranslateInstance(
				-hitbox.getWidth() / 2, -hitbox.getHeight() / 2));
		res.transform(AffineTransform.getRotateInstance(getSpeed()
				.getDirection()));
		res.transform(AffineTransform.getTranslateInstance(
				getLocation().getX(), getLocation().getY()));
		return res;
	}
}
