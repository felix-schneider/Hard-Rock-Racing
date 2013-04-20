package scaatis.rrr;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import scaatis.util.CollisionTools;
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

	public static final double frontFriction = 100;
	public static final double sidewaysFriction = 500;

	public static final double topSpeed = 200;

	public static final double acceleration = 140 + frontFriction;

	/**
	 * Turning speed in radians/second
	 */
	public static final double turningSpeed = .35 * Math.PI;

	/**
	 * Minimum speed in units/second the car must have for full turning speed
	 */
	public static final double minSpeed = 50;

	public static final double collisionRotation = Math.toRadians(15);

	public static final double collisionRepulsion = 40;

	public static final double collisionCooldown = .5;

	private static final double epsilon = 10e-5;

	private Point2D location;
	private Vector2D speed;
	private double facing;

	private int accelerating; // 0 not accelerating, 1 forward, 2 backward
	private int turning; // 0 - not turning, > 0 turning from positive x to
							// positive y, < 0 the other way
	private double cooldown;

	public Car() {
		this(new Point(), 0);
	}

	public Car(Point2D location, double facing) {
		this.location = new Point2D.Double(location.getX(), location.getY());
		this.facing = facing;
		speed = new Vector2D.Polar(0, 0);
		accelerating = 0;
		turning = 0;
		cooldown = 0;
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

	public int getAccelerating() {
		return accelerating;
	}

	public void setAccelerating(int accel) {
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
		if (cooldown > epsilon) {
			cooldown -= delta;
		}

		if (turning != 0) {
			facing += Math.signum(turning) * turningSpeed
					* Math.min(1, speed.getMagnitude() / minSpeed) * delta;
		}

		Vector2D accel = new Vector2D.Cartesian(0, 0);

		// car acceleration
		if (accelerating > 0) {
			accel = accel.add(new Vector2D.Polar(facing, acceleration));
		} else if (accelerating < 0) {
			accel = accel.subtract(new Vector2D.Polar(facing, acceleration));
		}

		// friction and drag
		if (speed.getMagnitude() > epsilon) {
			double angle = facing - speed.getDirection();
			double magnitude = Math.abs(Math.sin(angle))
					* (sidewaysFriction - frontFriction) + frontFriction;
			accel = accel.add(new Vector2D.Polar(
					speed.getDirection() - Math.PI, magnitude));
		}

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

	public void collideWith(Track other) {
		Point2D center = new Point2D.Double(other.getTrackArea().getBounds2D()
				.getCenterX(), other.getTrackArea().getBounds2D().getCenterY());
		Vector2D fromCenter = new Vector2D.Cartesian(center, location);
		double bounceDirection = fromCenter.getDirection();
		int turndir = -1;
		Area test = getArea();
		test.intersect(other.getOuterArea());
		if (!test.isEmpty()) {
			bounceDirection += Math.PI;
			turndir = 1;
		}
		Vector2D facingVector = new Vector2D.Polar(facing, 10);
		Line2D facingLine = new Line2D.Double(location,
				facingVector.applyTo(location));
		turndir *= CollisionTools.isRight(facingLine, center);

		// turn the car, but only if that doesn't cause another collision
		double oldFacing = facing;
		facing += turndir * collisionRotation;
		test = getArea();
		test.intersect(other.getNegative());
		if (!test.isEmpty()) {
			facing = oldFacing;
		}

		// brake the car
		speed = speed.scale(Math.abs(Math.sin(speed.angleBetween(fromCenter))));

		// bounce
		speed = speed.add(new Vector2D.Polar(bounceDirection,
				collisionRepulsion));
	}

	public static void collide(Car one, Car other) {
		if (one.cooldown > epsilon && other.cooldown > epsilon) {
			return;
		}
		one.cooldown = collisionCooldown;
		other.cooldown = collisionCooldown;
		double speedDiff = one.speed.subtract(other.speed).getMagnitude();
		Vector2D ab = new Vector2D.Polar(new Vector2D.Cartesian(one.location,
				other.location).getDirection(), speedDiff);
		one.speed = one.speed.subtract(ab);
		other.speed = other.speed.add(ab);
	}
}
