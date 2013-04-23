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
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.rrr.tracktiles.TrackTile;

public class Track implements Collides, JSONable {
	private TrackTile[] tiles;
	private BufferedImage image;
	private Area track;
	private FinishLine finishLine;
	private List<CheckPoint> checkPoints;
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
		checkPoints = new ArrayList<>();
		finishLine = null;

		for (TrackTile tile : tiles) {
			if (tile instanceof FinishLine) {
				if (finishLine == null) {
					finishLine = (FinishLine) tile;
				} else {
					throw new IllegalArgumentException(
							"Track contains several start/finish lines.");
				}
			}
			if (tile instanceof CheckPoint) {
				checkPoints.add((CheckPoint) tile);
			}
			tile.calcLocation(state);
			state = tile.getConnect(state.getDirection());
		}
		if (!state.equals(startstate)) {
			throw new IllegalArgumentException("Track is not a closed circuit.");
		}
		if (finishLine == null) {
			throw new IllegalArgumentException(
					"Track contains no start/finish line.");
		}
		if (checkPoints.size() <= 1) {
			throw new IllegalArgumentException("Track contains no checkpoints.");
		}
		this.startDir = startDir;
		this.tiles = tiles;
		bake();
		makeNegative();
	}

	@Override
	public Area getArea() {
		return getNegative();
	}

	public Area getTrackArea() {
		return track;
	}

	public FinishLine getFinishLine() {
		return finishLine;
	}

	public List<CheckPoint> getCheckpoints() {
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
		for (TrackTile t : tiles) {
			track.add(t.getArea());
		}
		Rectangle bounds = track.getBounds();

		track.transform(AffineTransform.getTranslateInstance(-bounds.x,
				-bounds.y));
		// calculate final location for tiles
		TrackState state = new TrackState(new Point((int) -bounds.getX(),
				(int) -bounds.getY()), startDir);
		for (TrackTile t : tiles) {
			t.calcLocation(state);
			state = t.getConnect(state.getDirection());
		}
		track = new Area();
		image = new BufferedImage(bounds.width, bounds.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setColor(Color.white);
		for (TrackTile t : tiles) {
			track.add(t.getArea());
			g.fill(t.getArea());
		}
		g.setColor(Color.green);
		g.fill(finishLine.getArea());
		g.setColor(Color.yellow);
		for (CheckPoint checkPoint : checkPoints) {
			if (checkPoint != finishLine) {
				g.fill(checkPoint.getArea());
			}
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

	@Override
	public JSONObject toJSON() {
		return toJSON(false);
	}

	public JSONObject toJSON(boolean asTiles) {
		JSONObject obj = new JSONObject();
		obj.put("message", "track");
		obj.put("tiled", asTiles);
		if (asTiles) {
			List<JSONObject> tiled = new ArrayList<>();
			for (TrackTile tile : tiles) {
				tiled.add(tile.toJSON());
			}
			obj.put("tiles", tiled);
		} else {
			obj.put("width", image.getWidth());
			obj.put("height", image.getHeight());
			obj.put("startdir", startDir.toString());
			obj.put("values", Arrays.asList(((DataBufferByte) image.getRaster()
					.getDataBuffer()).getData()));
		}
		return obj;
	}
}
