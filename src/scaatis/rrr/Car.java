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
    public static final double      sidewaysFriction   = 500;
    public static final double      frontFriction      = 100;
    
    public static final double      acceleration       = 140 + frontFriction;
    public static final double      boostAcceleration  = acceleration * 3;
    public static final double      topSpeed           = 200;
    public static final double      boostSpeed         = topSpeed * 2;
    
    public static final double      boostDuration      = 1;
    
    public static final double      turningSpeed       = .35 * Math.PI;
    
    public static final double      collisionCooldown  = .5;
    public static final double      collisionRepulsion = 40;
    public static final double      collisionRotation  = Math.toRadians(15);
    
    /**
     * Minimum speed difference so that a player will take damage in a collision
     */
    public static final double      damageThreshHold   = 90;
    
    /**
     * The hitbox of the car when facing positive x
     */
    public static final Rectangle2D hitbox             = new Rectangle2D.Double(0, 0, 60,
                                                               45);
    
    public static final int         maxHP              = 6;
    /**
     * Minimum speed in units/second the car must have for full turning speed
     */
    public static final double      minSpeed           = 50;
    
    private static final double     epsilon            = 10e-5;
    private static int              idtotal            = 1;
    
    private int    accelerating; // 0 not accelerating, 1 forward, 2 backward
    private double boost;       // boost duration
                                 // positive y, < 0 the other way
    private double cooldown;    // cooldown for car/car collision
    private double facing;
    private int    hp;
    private int    id;
    private double mine;        // mine stun duration
                                 
    private int    turning;     // 0 - not turning, > 0 turning from positive x
                                 // to
                                 
    public Car() {
        this(new Point(), 0);
    }
    
    public Car(Point2D location, Direction facing) {
        this(location, facing.getAngle());
    }
    
    public Car(Point2D location, double facing) {
        super(location, new Vector2D.Polar(0, 0));
        this.facing = facing;
        accelerating = 0;
        turning = 0;
        cooldown = 0;
        hp = maxHP;
        id = idtotal++;
        boost = 0;
        mine = 0;
    }
    
    public void addCheckPointListener(CheckPointListener listener) {
        getListeners().add(CheckPointListener.class, listener);
    }
    
    public void boost() {
        boost = boostDuration;
    }
    
    public void collideWith(CheckPoint other) {
        CheckPointListener[] ls = getListeners().getListeners(
                CheckPointListener.class);
        CheckPointEvent event = new CheckPointEvent(this, other);
        for (CheckPointListener listener : ls) {
            listener.checkPoint(event);
        }
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
    
    public boolean damage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            stopTurning();
            setAccelerating(0);
            destroy();
        }
        return hp <= 0;
    }
    
    public int getAccelerating() {
        return accelerating;
    }
    
    @Override
    public Area getArea() {
        Area res = new Area(hitbox);
        res.transform(AffineTransform.getTranslateInstance(
                -hitbox.getWidth() / 2, -hitbox.getHeight() / 2));
        res.transform(AffineTransform.getRotateInstance(facing));
        res.transform(AffineTransform.getTranslateInstance(
                getLocation().getX(), getLocation().getY()));
        return res;
    }
    
    public double getFacing() {
        return facing;
    }
    
    public int getHP() {
        return hp;
    }
    
    public int getID() {
        return id;
    }
    
    public Point getIntLocation() {
        return new Point((int) getLocation().getX(), (int) getLocation().getY());
    }
    
    public int getTurning() {
        return turning;
    }
    
    public boolean isBoosting() {
        return boost > 0;
    }
    
    public boolean isStunned() {
        return mine > 0;
    }
    
    public void mine() {
        mine = Mine.disableDuration;
        setSpeed(new Vector2D.Polar(facing, 0));
    }
    
    public void removeCheckPointListener(CheckPointListener listener) {
        getListeners().remove(CheckPointListener.class, listener);
    }
    
    public void setAccelerating(int accel) {
        accelerating = accel;
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
    
    public void setTurning(int turn) {
        turning = turn;
    }
    
    public void stopTurning() {
        setTurning(0);
    }
    
    @Override
    public void update(double delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
        
        if (mine > 0) {
            mine -= delta;
            return;
        }
        
        if (turning != 0) {
            facing += Math.signum(turning) * turningSpeed
                    * Math.min(1, getSpeed().getMagnitude() / minSpeed) * delta;
        }
        
        Vector2D accel = new Vector2D.Cartesian(0, 0);
        
        // car acceleration
        if (isBoosting()) {
            accel = accel.add(new Vector2D.Polar(facing, boostAcceleration));
        } else if (accelerating > 0) {
            accel = accel.add(new Vector2D.Polar(facing, acceleration));
        }
        
        // friction and drag
        if (getSpeed().getSquareMagnitude() > epsilon * epsilon) {
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
        if (!isBoosting()
                && getSpeed().getSquareMagnitude() > topSpeed * topSpeed) {
            setSpeed(new Vector2D.Polar(getSpeed().getDirection(), topSpeed));
        } else if (isBoosting()
                && getSpeed().getSquareMagnitude() > boostSpeed * boostSpeed) {
            setSpeed(new Vector2D.Polar(getSpeed().getDirection(), boostSpeed));
        }
        loc = loc.add(oldSpeed.add(getSpeed()).scale(delta));
        
        setLocation(new Point2D.Double(loc.getX(), loc.getY()));
        
        if (boost > 0) {
            boost -= delta;
        }
    }
    
    
    public static void collide(Car one, Car other) {
        if (one.cooldown > 0 && other.cooldown > 0) {
            return;
        }
        one.cooldown = collisionCooldown;
        other.cooldown = collisionCooldown;
        double onespeed = one.getSpeed().getMagnitude();
        double otherspeed = other.getSpeed().getMagnitude();
        double fasterspeed = Math.max(onespeed, otherspeed);
        double slowerspeed = Math.min(onespeed, otherspeed);
        // if the cars are headed towards each other - add the speeds
        // if they are going the same way - subtract
        // if they are going perpendicular to eacth other, take the faster one
        double speedDiff = fasterspeed
                - Math.cos(one.getSpeed().angleBetween(other.getSpeed())) * slowerspeed;
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
}
