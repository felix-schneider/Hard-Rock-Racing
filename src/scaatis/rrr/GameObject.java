package scaatis.rrr;

import java.awt.geom.Point2D;

import javax.swing.event.EventListenerList;

import org.json.JSONObject;

import scaatis.rrr.event.DestroyedEvent;
import scaatis.rrr.event.DestroyedListener;
import scaatis.util.Vector2D;

public class GameObject implements JSONable {
    private static int        idcounter = 1;

    private boolean           destroyed;
    private EventListenerList listeners;
    private Point2D           location;
    private Vector2D          speed;
    private int               id;

    protected GameObject(Point2D location, Vector2D speed) {
        this.location = location;
        this.speed = speed;
        listeners = new EventListenerList();
        destroyed = false;
        id = idcounter++;
    }

    public void addDestroyedListener(DestroyedListener listener) {
        listeners.add(DestroyedListener.class, listener);
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

    public Point2D getLocation() {
        return location;
    }

    public Vector2D getSpeed() {
        return speed;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void removeDestroyedListener(DestroyedListener listener) {
        listeners.remove(DestroyedListener.class, listener);
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public void setSpeed(Vector2D speed) {
        this.speed = speed;
    }
    
    public int getID() {
        return id;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getID());
        obj.put("locationX", getLocation().getX());
        obj.put("locationY", getLocation().getY());
        obj.put("speedX", getSpeed().getX());
        obj.put("speedY", getSpeed().getY());
        return obj;
    }

    public void update(double delta) {
        location = speed.scale(delta).applyTo(location);
    }

    protected EventListenerList getListeners() {
        return listeners;
    }
}
