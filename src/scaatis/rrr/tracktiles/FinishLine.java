package scaatis.rrr.tracktiles;

import java.awt.geom.Point2D;

import scaatis.rrr.Direction;
import scaatis.util.Vector2D;

public class FinishLine extends CheckPoint {

    public FinishLine(Direction orientation) {
        super(orientation);
    }

    public Point2D getStartLocation(int pos) {
        int w = TRACK_WIDTH / 4;
        Point2D start = new Vector2D.Polar(getOrientation().cclockwise().getAngle(),
                TRACK_WIDTH / 2 - TRACK_WIDTH / 8).applyTo(getSpawnLocation());
        Vector2D diff = new Vector2D.Cartesian(
                new Vector2D.Polar(getOrientation().clockwise().getAngle(), w));
        for (int i = 1; i < pos; i++) {
            start = diff.applyTo(start);
        }
        return start;
    }
}
