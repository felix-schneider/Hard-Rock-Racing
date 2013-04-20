package scaatis.rrr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.rrr.tracktiles.TrackTile;

public class Track {
	private TrackTile[] tiles;
	private BufferedImage image;
	private Area track;
	private Area finishLine;
	private List<Area> checkPoints;
	private Direction startDir;
	private Area innerArea;
	private Area outerArea;
	private Area negative;

	public Track(Direction startDir, List<TrackTile> tiles) {
		this(startDir, convert(tiles));
	}

	private static TrackTile[] convert(List<TrackTile> tiles) {
		TrackTile[] tiles2 = new TrackTile[tiles.size()];
		tiles.toArray(tiles2);
		return tiles2;
	}

	public Track(Direction startDir, TrackTile... tiles) {
		TrackState startstate = new TrackState(new Point(), startDir);
		TrackState state = startstate;
		boolean hasFinish = false;
		int checkpoints = 0;

		for (TrackTile tile : tiles) {
			if (tile instanceof FinishLine) {
				if (!hasFinish) {
					hasFinish = true;
				} else {
					throw new IllegalArgumentException(
							"Track contains several start/finish lines.");
				}
			} else if (tile instanceof CheckPoint) {
				checkpoints++;
			}
			tile.calcLocation(state);
			state = tile.getConnect(state.getDirection());
		}
		if (!state.equals(startstate)) {
			throw new IllegalArgumentException("Track is not a closed circuit.");
		}
		if (!hasFinish) {
			throw new IllegalArgumentException(
					"Track contains no start/finish line.");
		}
		if (checkpoints == 0) {
			throw new IllegalArgumentException("Track contains no checkpoints.");
		}
		this.startDir = startDir;
		this.tiles = tiles;
		bake();
		makeNegative();
	}

	public Area getTrackArea() {
		return track;
	}

	public Area getFinishLine() {
		return finishLine;
	}

	public List<Area> getCheckpoints() {
		return new ArrayList<>(checkPoints);
	}

	public BufferedImage getTrackImage() {
		return image;
	}

	public Direction getStartDirection() {
		return startDir;
	}

	public Area getInnerArea() {
		return innerArea;
	}

	public Area getOuterArea() {
		return outerArea;
	}

	public Area getNegative() {
		return negative;
	}

	private void bake() {
		track = new Area();
		finishLine = new Area();
		checkPoints = new ArrayList<>();
		for (TrackTile t : tiles) {
			Area a = new Area(t.getShape());
			track.add(a);
			if (t instanceof FinishLine) {
				finishLine.add(a);
			} else if (t instanceof CheckPoint) {
				checkPoints.add(a);
			}
		}
		Rectangle bounds = track.getBounds();
		track.transform(AffineTransform.getTranslateInstance(-bounds.x,
				-bounds.y));
		finishLine.transform(AffineTransform.getTranslateInstance(-bounds.x,
				-bounds.y));
		for (Area checkPoint : checkPoints) {
			checkPoint.transform(AffineTransform.getTranslateInstance(
					-bounds.x, -bounds.y));
		}
		image = new BufferedImage(bounds.width, bounds.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setColor(Color.white);
		g.fill(track);
		g.setColor(Color.green);
		g.fill(finishLine);
		g.setColor(Color.yellow);
		for (Area checkPoint : checkPoints) {
			g.fill(checkPoint);
		}
		g.dispose();
	}

	private void makeNegative() {
		PathIterator iterator = track.getPathIterator(null);
		Path2D pathA = new Path2D.Double();
		Path2D pathB = new Path2D.Double();
		Path2D current = pathA;
		boolean firstDone = false;
		for (; !iterator.isDone(); iterator.next()) {
			double[] pts = new double[6];
			int type = iterator.currentSegment(pts);
			if (type == PathIterator.SEG_MOVETO) {
				if (!firstDone) {
					firstDone = true;
				} else {
					current = pathB;
				}
				current.moveTo(pts[0], pts[1]);
			} else if (type == PathIterator.SEG_LINETO) {
				current.lineTo(pts[0], pts[1]);
			} else if (type == PathIterator.SEG_QUADTO) {
				current.quadTo(pts[0], pts[1], pts[2], pts[3]);
			} else if (type == PathIterator.SEG_CUBICTO) {
				current.curveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
			} else if (type == PathIterator.SEG_CLOSE) {
				current.closePath();
			}
		}
		Area areaA = new Area(pathA);
		Area areaB = new Area(pathB);
		Area areaC = new Area(areaA);
		areaC.intersect(areaB);
		if (areaC.equals(areaB)) {
			innerArea = areaB;
			outerArea = areaA;
		} else if (areaC.equals(areaA)) {
			innerArea = areaA;
			outerArea = areaB;
		} else {
			throw new IllegalStateException();
		}
		Rectangle bounds = new Rectangle(track.getBounds());
		bounds.grow(10, 10);
		Area boundsArea = new Area(bounds);
		boundsArea.subtract(outerArea);
		outerArea = boundsArea;
		negative = new Area(boundsArea);
		negative.add(innerArea);
	}
}
