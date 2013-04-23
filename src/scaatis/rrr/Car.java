package scaatis.rrr;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import scaatis.rrr.event.CheckPointEvent;
import scaatis.rrr.event.CheckPointListener;
import scaatis.rrr.tracktiles.CheckPoint;
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
public class Car extends GameObject implements Collides {
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

	/**
	 * Minimum speed difference so that a player will take damage in a collision
	 */
	public static final double damageThreshHold = 90;
	public static final int maxHP = 6;

	private static final double epsilon = 10e-5;

	private double facing;

	private int accelerating; // 0 not accelerating, 1 forward, 2 backward
	private int turning; // 0 - not turning, > 0 turning from positive x to
							// positive y, < 0 the other way
	private double cooldown;
	private int hp;

	public Car() {
		this(new Point(), 0);
	}

	public Car(Point2D location, double facing) {
		super(location, new Vector2D.Polar(0, 0));
		this.facing = facing;
		accelerating = 0;
		turning = 0;
		cooldown = 0;
		hp = maxHP;
	}

	public Car(Point2D location, Direction facing) {
		this(location, facing.getAngle());
	}

	@Override
	public void update(double delta) {
		if (cooldown > epsilon) {
			cooldown -= delta;
		}

		if (turning != 0) {
			facing += Math.signum(turning) * turningSpeed
					* Math.min(1, getSpeed().getMagnitude() / minSpeed) * delta;
		}

		Vector2D accel = new Vector2D.Cartesian(0, 0);

		// car acceleration
		if (accelerating > 0) {
			accel = accel.add(new Vector2D.Polar(facing, acceleration));
		} else if (accelerating < 0) {
			accel = accel.subtract(new Vector2D.Polar(facing, acceleration));
		}

		// friction and drag
		if (getSpeed().getMagnitude() > epsilon) {
			double angle = facing - getSpeed().getDirection();
			double magnitude = Math.abs(Math.sin(angle))
					* (sidewaysFriction - frontFriction) + frontFriction;
			accel = accel.add(new Vector2D.Polar(getSpeed().getDirection()
					- Math.PI, magnitude));
		}

		Vector2D loc = new Vector2D.Cartesian(getLocation().getX(),
				getLocation().getY());
		Vector2D oldSpeed = getSpeed();
		setSpeed(getSpeed().add(accel.scale(delta)));
		if (getSpeed().getMagnitude() > topSpeed) {
			setSpeed(new Vector2D.Polar(getSpeed().getDirection(), topSpeed));
		}
		loc = loc.add(oldSpeed.add(getSpeed()).scale(delta));

		setLocation(new Point2D.Double(loc.getX(), loc.getY()));
	}

	public Area getArea() {
		Area res = new Area(hitbox);
		res.transform(AffineTransform.getTranslateInstance(
				-hitbox.getWidth() / 2, -hitbox.getHeight() / 2));
		res.transform(AffineTransform.getRotateInstance(facing));
		res.transform(AffineTransform.getTranslateInstance(
				getLocation().getX(), getLocation().getY()));
		return res;
	}

	public void collideWith(Track other) {
		Point2D center = new Point2D.Double(other.getTrackArea().getBounds2D()
				.getCenterX(), other.getTrackArea().getBounds2D().getCenterY());
		Vector2D fromCenter = new Vector2D.Cartesian(center, getLocation());
		double bounceDirection = fromCenter.getDirection();
		int turndir = -1;
		Area test = getArea();
		test.intersect(other.getOuterArea());
		if (!test.isEmpty()) {
			bounceDirection += Math.PI;
			turndir = 1;
		}
		Vector2D facingVector = new Vector2D.Polar(facing, 10);
		Line2D facingLine = new Line2D.Double(getLocation(),
				facingVector.applyTo(getLocation()));
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
		setSpeed(getSpeed().scale(
				Math.abs(Math.sin(getSpeed().angleBetween(fromCenter)))));

		// bounce
		setSpeed(getSpeed().add(
				new Vector2D.Polar(bounceDirection, collisionRepulsion)));
	}

	public void collideWith(CheckPoint other) {
		CheckPointListener[] ls = getListeners().getListeners(
				CheckPointListener.class);
		CheckPointEvent event = new CheckPointEvent(this, other);
		for (CheckPointListener listener : ls) {
			listener.checkPoint(event);
		}
	}

	public static void collide(Car one, Car other) {
		if (one.cooldown > epsilon && other.cooldown > epsilon) {
			return;
		}
		one.cooldown = collisionCooldown;
		other.cooldown = collisionCooldown;
		double speedDiff = one.getSpeed().subtract(other.getSpeed())
				.getMagnitude();
		Vector2D ab = new Vector2D.Polar(new Vector2D.Cartesian(
				one.getLocation(), other.getLocation()).getDirection(),
				speedDiff);
		one.setSpeed(one.getSpeed().subtract(ab));
		other.setSpeed(other.getSpeed().add(ab));
		if (speedDiff > damageThreshHold) {
			Car slower;
			if (one.getSpeed().getMagnitude() < other.getSpeed().getMagnitude()) {
				slower = one;
			} else {
				slower = other;
			}
			slower.damage((int) (speedDiff / damageThreshHold));
		}
	}

	public boolean damage(int damage) {
		hp -= damage;
		if (hp <= 0) {
			stopTurning();
			setAccelerating(0);
			destroy();
		}
		return hp <= 0;
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

	public void addCheckPointListener(CheckPointListener listener) {
		getListeners().add(CheckPointListener.class, listener);
	}

	public void removeCheckPointListener(CheckPointListener listener) {
		getListeners().remove(CheckPointListener.class, listener);
	}

	public void stopTurning() {
		setTurning(0);
	}

	public Point getIntLocation() {
		return new Point((int) getLocation().getX(), (int) getLocation().getY());
	}

	public int getAccelerating() {
		return accelerating;
	}

	public double getFacing() {
		return facing;
	}

	public void setAccelerating(int accel) {
		accelerating = accel;
	}

	public void setTurning(int turn) {
		turning = turn;
	}

	public int getHP() {
		return hp;
	}
}
