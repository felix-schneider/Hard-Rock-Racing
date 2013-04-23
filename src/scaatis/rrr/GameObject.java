package scaatis.rrr;

import java.awt.geom.Point2D;

import javax.swing.event.EventListenerList;

import scaatis.rrr.event.DestroyedEvent;
import scaatis.rrr.event.DestroyedListener;
import scaatis.util.Vector2D;

public class GameObject {
	private Point2D location;
	private Vector2D speed;
	private EventListenerList listeners;
	private boolean destroyed;

	protected GameObject(Point2D location, Vector2D speed) {
		this.location = location;
		this.speed = speed;
		listeners = new EventListenerList();
		destroyed = false;
	}

	public void addDestroyedListener(DestroyedListener listener) {
		listeners.add(DestroyedListener.class, listener);
	}

	public void removeDestroyedListener(DestroyedListener listener) {
		listeners.remove(DestroyedListener.class, listener);
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
		DestroyedListener[] ls = listeners
				.getListeners(DestroyedListener.class);
		DestroyedEvent e = new DestroyedEvent(this);
		for (DestroyedListener listener : ls) {
			listener.destroyed(e);
		}
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}
	
	protected EventListenerList getListeners() {
		return listeners;
	}
}
