package scaatis.rrr.tracktiles;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import scaatis.rrr.Direction;
import scaatis.rrr.TrackState;

public class Straight extends TrackTile {

	private static final Rectangle vertical = new Rectangle(TRACK_WIDTH,
			SEGMENT_LENGTH);
	private static final Rectangle horizontal = new Rectangle(SEGMENT_LENGTH,
			TRACK_WIDTH);

	public Straight(Direction orientation) {
		super(orientation);
	}

	@Override
	public Shape getShape() {
		int x = getLocation().x;
		int y = getLocation().y;
		Rectangle rect;
		if (getOrientation() == Direction.UP
				|| getOrientation() == Direction.DOWN) {
			rect = new Rectangle(vertical);
		} else {
			rect = new Rectangle(horizontal);
		}
		rect.setLocation(x, y);
		return rect;
	}

	@Override
	public boolean checkDirection(Direction direction) {
		return direction == getOrientation()
				|| direction == getOrientation().opposite();
	}

	@Override
	public Point getRelativeConnect(Direction direction) {
		if (!checkDirection(direction)) {
			throw new IllegalArgumentException("Cannot move "
					+ direction.toString() + " on a straight going "
					+ getOrientation().toString() + ".");
		}
		Point pos = new Point();
		if (getOrientation() == Direction.UP
				|| getOrientation() == Direction.DOWN) {
			if (direction == Direction.DOWN) {
				pos.translate(0, SEGMENT_LENGTH);
			}
		} else {
			if (direction == Direction.RIGHT) {
				pos.translate(SEGMENT_LENGTH, 0);
			}
		}
		return pos;
	}

	@Override
	public void calcLocation(TrackState state) {
		if (!checkDirection(state.getDirection())) {
			throw new IllegalArgumentException("Cannot move "
					+ state.getDirection().toString() + " on a straight going "
					+ getOrientation().toString() + ".");
		}
		if (getOrientation() == Direction.UP
				|| getOrientation() == Direction.DOWN) {
			if (state.getDirection() == Direction.UP) {
				setLocation(state.getLocation().x, state.getLocation().y
						- SEGMENT_LENGTH);
			} else {
				setLocation(state.getLocation());
			}
		} else {
			if (state.getDirection() == Direction.LEFT) {
				setLocation(state.getLocation().x - SEGMENT_LENGTH,
						state.getLocation().y);
			} else {
				setLocation(state.getLocation());
			}
		}
	}

	@Override
	public TrackState getConnect(Direction direction) {
		Point res = new Point(getLocation());
		Point rel = getRelativeConnect(direction);
		res.translate(rel.x, rel.y);
		return new TrackState(res, direction);
	}
}
