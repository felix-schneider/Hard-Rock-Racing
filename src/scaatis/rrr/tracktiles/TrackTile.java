package scaatis.rrr.tracktiles;

import java.awt.Point;
import java.awt.Shape;

import scaatis.rrr.Direction;
import scaatis.rrr.TrackState;

public abstract class TrackTile {
	
	public static final int SEGMENT_LENGTH = 45;
	public static final int TRACK_WIDTH = SEGMENT_LENGTH * 5;
	
	private Point location;
	private Direction orientation;

	protected TrackTile(Direction orientation) {
		this.orientation = orientation;
	}

	public abstract Shape getShape();
	public abstract TrackState getConnect(Direction direction);
	public abstract Point getRelativeConnect(Direction direction);
	public abstract boolean checkDirection(Direction direction);
	public abstract void calcLocation(TrackState state);
	
	public Point getLocation() {
		return location;
	}
	
	protected void setLocation(Point p) {
		this.location = new Point(p);
	}
	
	protected void setLocation(int x, int y) {
		this.location = new Point(x, y);
	}
	
	public Direction getOrientation() {
		return orientation;
	}
	
	
}
