package scaatis.rrr.tracktiles;

import java.awt.geom.Point2D;

import scaatis.rrr.Car;
import scaatis.rrr.Direction;
import scaatis.util.Vector2D;

public class FinishLine extends CheckPoint {

	public FinishLine(Direction orientation) {
		super(orientation);
	}

	public Point2D getStartLocation(int pos) {
		int w = TRACK_WIDTH / 4;
		int offset = (w - (int) Car.hitbox.getHeight()) / 2;
		return new Vector2D.Polar(getOrientation().cclockwise().getAngle(),
				(3 - pos) * w - offset).applyTo(getSpawnLocation());

	}
}
