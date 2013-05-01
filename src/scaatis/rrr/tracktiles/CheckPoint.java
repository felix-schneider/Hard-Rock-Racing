package scaatis.rrr.tracktiles;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.json.JSONObject;

import scaatis.rrr.Collides;
import scaatis.rrr.Direction;

public class CheckPoint extends Straight implements Collides {

	public CheckPoint(Direction orientation) {
		super(orientation);
	}
	
	public Point2D getSpawnLocation() {
		Rectangle rectangle = getArea().getBounds();
		return new Point2D.Double(rectangle.getCenterX(), rectangle.getCenterY());
	}
	
	public JSONObject toJSON() {
		JSONObject obj = super.toJSON();
		obj.put("type", Straight.class.getSimpleName());
		return obj;
	}
}
