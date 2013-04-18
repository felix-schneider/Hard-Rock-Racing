package scaatis.rrr;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

public class TileImage {

	private Collection<Tile> images;
	private Rectangle bound;
	private int offsx;
	private int offsy;

	public TileImage(BufferedImage start) {
		images = new ArrayList<>();
		add(start, 0, 0);
	}

	public void add(BufferedImage img, int x, int y) {
		images.add(new Tile(x, y, img));
		updateRectangle();
	}

	public int getWidth() {
		return bound.width;
	}

	public int getHeight() {
		return bound.height;
	}

	private void updateRectangle() {
		bound = new Rectangle();
		for (Tile tile : images) {
			bound.add(new Rectangle(tile.x, tile.y, tile.img.getWidth(),
					tile.img.getHeight()));
		}
		offsx = -bound.x;
		offsy = -bound.y;
	}

	public BufferedImage bake() {
		BufferedImage img = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		for (Tile tile : images) {
			g.drawImage(tile.img, tile.x + offsx, tile.y + offsy, null);
		}
		return img;
	}

	private class Tile {
		public final int x;
		public final int y;
		public final BufferedImage img;

		public Tile(int x, int y, BufferedImage img) {
			this.x = x;
			this.y = y;
			this.img = img;
		}
	}
}
