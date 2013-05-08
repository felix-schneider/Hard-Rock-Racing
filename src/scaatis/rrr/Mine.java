package scaatis.rrr;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.json.JSONObject;

import scaatis.util.Vector2D;

public class Mine extends GameObject implements Collides {

	public static final int damage = 1;
	public static final Ellipse2D hitbox = new Ellipse2D.Double(0, 0, 20, 20);
	public static final double disableDuration = .5;

	public Mine(Point2D location) {
		super(location, new Vector2D.Cartesian(0, 0));
	}

	public void collideWith(Car other) {
	    other.mine();
		other.damage(damage);
		destroy();
	}

	@Override
	public Area getArea() {
		Area res = new Area(hitbox);
		res.transform(AffineTransform.getTranslateInstance(
				-hitbox.getWidth() / 2, -hitbox.getHeight() / 2));
		res.transform(AffineTransform.getTranslateInstance(
				getLocation().getX(), getLocation().getY()));
		return res;
	}

	@Override
	public void update(double delta) {

	}

	@Override
	public JSONObject toJSON() {
		JSONObject obj = super.toJSON();
		obj.put("message", "mine");
		return obj;
	}
}
