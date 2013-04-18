package scaatis.rrr;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import scaatis.util.Vector2D;

/**
 * This class models a car in the race.
 * 
 * The car accelerates in the direction it is facing with a given force. It is
 * slowed down, i.e. a force works in the opposite direction of its motion by
 * friction. This force is greater depending on the angle between the car's
 * facing and the direction it is moving (going sideways has higher friction).
 * 
 * For simpler math, the mass of the car is defined as 1.
 * 
 * @author Felix Schneider
 * @version
 */
public class Car implements Updates {
	/**
	 * The hitbox of the car when facing positive x
	 */
	public static final Rectangle2D hitbox = new Rectangle2D.Double(0, 0, 60,
			45);

	public static final double topSpeed = 100;

	public static final double acceleration = 150;

	public static final double friction = 200;

	/**
	 * Turning speed in radians/second
	 */
	public static final double turningSpeed = .6 * Math.PI;

	private static final double epsilon = 10e-5;

	/**
	 * Minimum speed in units/second the car must have for full turning speed
	 */
	public static final double minSpeed = 2;

	private Point2D location;
	private Vector2D speed;
	private double facing;

	private boolean accelerating;
	private int turning; // 0 - not turning, > 0 turning from positive x to
							// positive y, < 0 the other way

	public Car() {
		this(new Point(), 0);
	}

	public Car(Point2D location, double facing) {
		this.location = new Point2D.Double(location.getX(), location.getY());
		this.facing = facing;
		speed = new Vector2D.Polar(0, 0);
		accelerating = false;
		turning = 0;
	}

	public Car(Point2D location, Direction facing) {
		this(location, facing.getAngle());
	}

	public Point2D getLocation() {
		return (Point2D) location.clone();
	}

	public Point getIntLocation() {
		return new Point((int) location.getX(), (int) location.getY());
	}

	public boolean isAccelerating() {
		return accelerating;
	}

	public void setAccelerating(boolean accel) {
		accelerating = accel;
	}

	public void setTurning(int turn) {
		turning = turn;
	}

	public void setTurning(Direction dir) {
		if (dir == Direction.LEFT) {
			turning = -1;
		} else if (dir == Direction.RIGHT) {
			turning = 1;
		} else {
			turning = 0;
		}
	}

	public void stopTurning() {
		turning = 0;
	}

	@Override
	public void update(double delta) {
		if (turning != 0) {
			facing += Math.signum(turning) * turningSpeed
					* Math.min(1, speed.getMagnitude() / minSpeed) * delta;
		}

		Vector2D accel = new Vector2D.Cartesian(0, 0);

		// car acceleration
		if (accelerating) {
			accel = accel.add(new Vector2D.Polar(facing, acceleration));
		}

		// friction and drag
		double angle = facing - speed.getDirection();
		double magnitude = Math.abs(Math.sin(angle)) * friction;
		accel = accel.add(new Vector2D.Polar(speed.getDirection() - Math.PI,
				magnitude));

		Vector2D loc = new Vector2D.Cartesian(location.getX(), location.getY());
		Vector2D oldSpeed = speed;
		speed = speed.add(accel.scale(delta));
		if (speed.getMagnitude() > topSpeed) {
			speed = new Vector2D.Polar(speed.getDirection(), topSpeed);
		}
		loc = loc.add(oldSpeed.add(speed).scale(delta));

		location = new Point2D.Double(loc.getX(), loc.getY());
	}

	public Area getArea() {
		Area res = new Area(hitbox);
		res.transform(AffineTransform.getTranslateInstance(
				-hitbox.getWidth() / 2, -hitbox.getHeight() / 2));
		res.transform(AffineTransform.getRotateInstance(facing));
		res.transform(AffineTransform.getTranslateInstance(location.getX(),
				location.getY()));
		return res;
	}
}
