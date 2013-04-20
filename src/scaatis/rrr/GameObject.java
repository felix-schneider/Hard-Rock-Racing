package scaatis.rrr;

import java.awt.geom.Point2D;

import scaatis.util.Vector2D;

public class GameObject {
	private Point2D location;
	private Vector2D speed;
	private boolean destroyed;
	
	protected GameObject(Point2D location, Vector2D speed) {
		this.location = location;
		this.speed = speed;
		destroyed = false;
	}
	
	public void update(double delta) {
		location = speed.scale(delta).applyTo(location);
	}

	public Point2D getLocation() {
		return location;
	}

	public void setLocation(Point2D location) {
		this.location = location;
	}

	public Vector2D getSpeed() {
		return speed;
	}

	public void setSpeed(Vector2D speed) {
		this.speed = speed;
	}
	
	public void destroy() {
		destroyed = true;
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}
}
