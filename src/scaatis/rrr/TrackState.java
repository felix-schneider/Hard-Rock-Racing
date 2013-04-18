package scaatis.rrr;

import java.awt.Point;

public class TrackState {
	private Point location;
	private Direction direction;

	public TrackState(int x, int y, Direction direction) {
		this.location = new Point(x, y);
		this.direction = direction;
	}

	public TrackState(Point location, Direction direction) {
		this(location.x, location.y, direction);
	}

	public Point getLocation() {
		return new Point(location);
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TrackState)) {
			return false;
		}
		TrackState other2 = (TrackState) other;
		return location.equals(other2.location)
				&& direction == other2.direction;
	}
}
