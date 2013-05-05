package scaatis.rrr.tracktiles;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import scaatis.rrr.Direction;
import scaatis.rrr.TrackState;

/**
 * Curve is always a right bend when moving in the direction orienation.
 * 
 * @author Felix Schneider
 * @version
 */
public class Curve extends TrackTile {

	/**
	 * curve with orientation Direction.DOWN
	 */
	private static Area curve = new Area(new Ellipse2D.Double(
			-(TRACK_WIDTH + SEGMENT_LENGTH), -(TRACK_WIDTH + SEGMENT_LENGTH),
			2 * (TRACK_WIDTH + SEGMENT_LENGTH),
			2 * (TRACK_WIDTH + SEGMENT_LENGTH)));
	static {
		curve.intersect(new Area(new Rectangle(0, 0, TRACK_WIDTH
				+ SEGMENT_LENGTH, TRACK_WIDTH + SEGMENT_LENGTH)));
		curve.subtract(new Area(new Ellipse2D.Double(-SEGMENT_LENGTH,
				-SEGMENT_LENGTH, 2 * SEGMENT_LENGTH, 2 * SEGMENT_LENGTH)));
	}

	public Curve(Direction orientation) {
		super(orientation);
	}

	@Override
	public Area getArea() {
		Area res;
		switch (getOrientation()) {
		case DOWN:
			res = new Area(curve);
			break;
		case LEFT:
			res = curve.createTransformedArea(AffineTransform
					.getQuadrantRotateInstance(1));
			res.transform(AffineTransform.getTranslateInstance(SEGMENT_LENGTH + TRACK_WIDTH, 0));
			break;
		case UP:
			res = curve.createTransformedArea(AffineTransform
					.getQuadrantRotateInstance(2));
			res.transform(AffineTransform.getTranslateInstance(TRACK_WIDTH
					+ SEGMENT_LENGTH, TRACK_WIDTH + SEGMENT_LENGTH));
			break;
		case RIGHT:
			res = curve.createTransformedArea(AffineTransform
					.getQuadrantRotateInstance(3));
			res.transform(AffineTransform.getTranslateInstance(0, TRACK_WIDTH + SEGMENT_LENGTH));
			break;
		default:
			res = null;
		}
		res.transform(AffineTransform.getTranslateInstance(getLocation().x,
				getLocation().y));
		return res;
	}

	@Override
	public boolean checkDirection(Direction direction) {
		return direction == getOrientation()
				|| direction == getOrientation().cclockwise();
	}

	@Override
	public Point getRelativeConnect(Direction direction) {
		if (!checkDirection(direction)) {
			throw new IllegalArgumentException("Cannot move "
					+ direction.toString() + " on a curve going from "
					+ getOrientation().opposite().toString() + " to "
					+ getOrientation().clockwise().toString() + ".");
		}
		switch (getOrientation()) {
		case DOWN:
			if (direction == Direction.DOWN) {
				return new Point(0, SEGMENT_LENGTH);
			} else {
				return new Point(SEGMENT_LENGTH, 0);
			}
		case RIGHT:
			if (direction == Direction.RIGHT) {
				return new Point(SEGMENT_LENGTH, TRACK_WIDTH + SEGMENT_LENGTH);
			} else {
				return new Point();
			}
		case UP:
			if (direction == Direction.UP) {
				return new Point(SEGMENT_LENGTH + TRACK_WIDTH, 0);
			} else {
				return new Point(0, SEGMENT_LENGTH + TRACK_WIDTH);
			}
		case LEFT:
			if (direction == Direction.LEFT) {
				return new Point();
			} else {
				return new Point(SEGMENT_LENGTH + TRACK_WIDTH, SEGMENT_LENGTH);
			}
		default:
			return null;
		}
	}

	@Override
	public void calcLocation(TrackState state) {
		Point rel;
		switch (getOrientation()) {
		case DOWN:
			rel = new Point(-SEGMENT_LENGTH, -SEGMENT_LENGTH);
			break;
		case RIGHT:
			rel = new Point(-SEGMENT_LENGTH, -TRACK_WIDTH - SEGMENT_LENGTH);
			break;
		case UP:
			rel = new Point(-SEGMENT_LENGTH - TRACK_WIDTH, -SEGMENT_LENGTH - TRACK_WIDTH);
			break;
		case LEFT:
			rel = new Point(-SEGMENT_LENGTH - TRACK_WIDTH, -SEGMENT_LENGTH);
			break;
		default:
			rel = null;
		}
		Point rel2 = getRelativeConnect(state.getDirection());
		setLocation(state.getLocation().x + rel.x + rel2.x,
				state.getLocation().y + rel.y + rel2.y);
	}

	@Override
	public TrackState getConnect(Direction direction) {
		Point res = new Point(getLocation());
		Point rel = getRelativeConnect(direction);
		res.translate(rel.x, rel.y);
		Direction outDir;
        if(direction == getOrientation()) {
            outDir = direction.clockwise();
        } else {
            outDir = direction.cclockwise();
        }
		return new TrackState(res, outDir);
	}

    @Override
    public String getDescription(Direction direction) { 
        if(direction == getOrientation()) {
            return "turnright";
        } else {
            return "turnleft";
        }
    }
	
	
}
