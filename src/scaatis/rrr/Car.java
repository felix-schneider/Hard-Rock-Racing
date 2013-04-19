package scaatis.rrr;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import scaatis.util.CollisionTools;
import scaatis.util.Side;
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
public class Car implements Updates, CollidesWith<Track> {
	/**
	 * The hitbox of the car when facing positive x
	 */
	public static final Rectangle2D hitbox = new Rectangle2D.Double(0, 0, 60,
			45);

	public static final double frontFriction = 100;
	public static final double sidewaysFriction = 400;

	public static final double topSpeed = 150;

	public static final double acceleration = 125 + frontFriction;

	/**
	 * Turning speed in radians/second
	 */
	public static final double turningSpeed = .35 * Math.PI;

	/**
	 * Minimum speed in units/second the car must have for full turning speed
	 */
	public static final double minSpeed = 50;

	public static final double collisionRotation = Math.toRadians(10);

	public static final double collisionRepulsion = 80;

	private static final double collisionCooldown = .06;

	private static final double epsilon = 10e-5;

	private Point2D location;
	private Vector2D speed;
	private double facing;

	private boolean accelerating;
	private int turning; // 0 - not turning, > 0 turning from positive x to
							// positive y, < 0 the other way
	private double ccooldown;

	public Car() {
		this(new Point(), 0);
	}

	public Car(Point2D location, double facing) {
		this.location = new Point2D.Double(location.getX(), location.getY());
		this.facing = facing;
		speed = new Vector2D.Polar(0, 0);
		accelerating = false;
		turning = 0;
		ccooldown = 0;
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
		if (ccooldown > epsilon) {
			ccooldown -= delta;
		}

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
	
	
	/**
	 * Handle a collision between this car and the track.
	 * 
	 * The following assumptions are made: The side of the intersection shape
	 * whose midpoint is closest to the car's center is the collision surface.
	 * 
	 * What happens is: The car is instantly turned away from the collision by a
	 * constant amount
	 * 
	 * The car's speed is reduced and possibly inverted, depending on the angle
	 * of collision
	 * 
	 * The car's speed is also increased away from the collision
	 */
	@Override
	public void collideWith(Track other, Shape intersection) {
		if (ccooldown > epsilon) {
			return;
		}
		ccooldown = collisionCooldown;
		List<Side> sides = CollisionTools.getSides(intersection);
		if(sides.size() < 3) {
			return;
		}
		Side collisionSide = CollisionTools.getClosestSide(sides, location);
		Point2D collisionPoint = collisionSide.getMidPoint();
		Vector2D collisionNormal = CollisionTools.getNormal(collisionSide,
				sides.get((sides.indexOf(collisionSide) + 1) % sides.size()),
				intersection);
		if (collisionNormal == null) {
			throw new RuntimeException();
		}
		Vector2D facingVector = new Vector2D.Polar(facing, 1);
		Line2D facingLine = new Line2D.Double(location.getX(), location.getY(),
				location.getX() + facingVector.getX(), location.getY()
						+ facingVector.getY());

		int turndir = -CollisionTools.isRight(facingLine, collisionPoint);
		Vector2D sideVector = new Vector2D.Polar(facing + 0.5 * Math.PI,
				hitbox.getHeight() + 2);
		Line2D sideLine = new Line2D.Double(location.getX(), location.getY(),
				location.getX() + sideVector.getX(), location.getY()
						+ sideVector.getY());
		if (CollisionTools.isRight(sideLine, collisionPoint) > 0) {
			turndir *= -1;
		}
		// turn the car
		facing += turndir * collisionRotation;
		// facing += facingLine.relativeCCW(collisionPoint) * collisionRotation;

		Vector2D collisionDirection = new Vector2D.Cartesian(
				collisionPoint.getX() - location.getX(), collisionPoint.getY()
						- location.getY());
		// brake the car - more for a frontal collision
		speed = speed.scale(Math.abs(Math.sin(facingVector
				.angleBetween(collisionDirection))));

		// bounce the car
		speed = speed.add(new Vector2D.Polar(collisionNormal.getDirection(),
				collisionRepulsion));

		// DEBUG:
		for (Side side : sides) {
			System.out.println(side.toString());
		}
		System.out.println();
		System.out.println("Collision side was " + collisionSide.toString());
		System.out.println("That is at index " + sides.indexOf(collisionSide));
		System.out.println("Next is at index "
				+ (sides.indexOf(collisionSide) + 1) % sides.size());
		System.out.println("Next in line is "
				+ sides.get((sides.indexOf(collisionSide) + 1) % sides.size())
						.toString());
		System.out.println("CollisionNormal: " + collisionNormal.toString());
		System.out.println("Turndir: " + turndir);
		System.out.println("------------------------------------------------------");
		//System.exit(0);
	}
}
