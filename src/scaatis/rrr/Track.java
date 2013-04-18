package scaatis.rrr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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

	public Track(Direction startDir, List<TrackTile> tiles) {
		this(startDir, (TrackTile[]) tiles.toArray());
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
}
