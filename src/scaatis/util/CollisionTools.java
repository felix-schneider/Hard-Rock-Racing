package scaatis.util;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollisionTools {

	public static List<Point2D> getMidPoints(List<Side> sides) {
		ArrayList<Point2D> midPoints = new ArrayList<>();
		for (Side side : sides) {
			midPoints.add(side.getMidPoint());
		}
		return midPoints;
	}

	public static Point2D getClosest(List<Point2D> points,
			final Point2D reference) {
		return Collections.min(points, new Comparator<Point2D>() {
			@Override
			public int compare(Point2D arg0, Point2D arg1) {
				return Double.compare(arg0.distance(reference),
						arg1.distance(reference));
			}
		});
	}

	public static Side getClosestSide(List<Side> sides, final Point2D reference) {
		if (sides.size() == 0) {
			return null;
		}
		int smallestIndex = 0;
		double smallestDistance = sides.get(0).getMidPoint()
				.distance(reference);
		for (int i = 1; i < sides.size(); i++) {
			double dist = sides.get(i).getMidPoint().distance(reference);
			if (dist < smallestDistance) {
				smallestDistance = dist;
				smallestIndex = i;
			}
		}
		return sides.get(smallestIndex);
	}

	public static Vector2D getNormal(Side side, Side side2, Shape object) {
		Point2D p1 = side.getP1();
		Point2D p2 = side.getP2();
		Point2D p3 = side2.getP1();
		Point2D p4 = side2.getP2();

		if (p2 == p3) {
			return null;
		}

		Vector2D ab = new Vector2D.Cartesian(p2, p1);
		Vector2D ac = new Vector2D.Cartesian(p3, p4);
		Vector2D n = new Vector2D.Cartesian(ab.getY(), -ab.getX());
		if (n.dotProduct(ac) > 0) {
			return n.scale(-1);
		}
		return n;
	}

	public static int isRight(Line2D line, Point2D c) {
		Vector2D ab = new Vector2D.Cartesian(line.getP1(), line.getP2());
		Vector2D b = new Vector2D.Cartesian(c.getY(), -c.getX());
		b = b.subtract(new Vector2D.Cartesian(line.getP1().getY(), -line
				.getP1().getX()));
		return (int) Math.signum(ab.dotProduct(b));
	}

	private static final double minLength = 1;
	private static final double minAngle = Math.toRadians(5);

	public static List<Side> getSides(Shape s) {
		ArrayList<Side> sides = new ArrayList<>();
		PathIterator iter = s.getPathIterator(null);
		double currentX = 0;
		double currentY = 0;
		double startX = 0;
		double startY = 0;
		boolean moveDone = false;
		Side lastSide = null;
		for (; !iter.isDone(); iter.next()) {
			double[] coords = new double[6];
			int currentSegment = iter.currentSegment(coords);
			Side side = null;
			if (currentSegment == PathIterator.SEG_CUBICTO) {
				side = new Side(new CubicCurve2D.Double(currentX, currentY,
						coords[2], coords[3], coords[4], coords[5], coords[0],
						coords[1]));
			} else if (currentSegment == PathIterator.SEG_QUADTO) {
				side = new Side(new QuadCurve2D.Double(currentX, currentY,
						coords[2], coords[3], coords[0], coords[1]));
			} else if (currentSegment == PathIterator.SEG_LINETO) {
				side = new Side(new Line2D.Double(currentX, currentY,
						coords[0], coords[1]));
			} else if (currentSegment == PathIterator.SEG_CLOSE) {
				side = new Side(new Line2D.Double(currentX, currentY, startX,
						startY));
			} else if (currentSegment == PathIterator.SEG_MOVETO) {
				if (moveDone) {
					break;
				} else {
					moveDone = true;
				}
				startX = coords[0];
				startY = coords[1];
			}
			boolean ok = false;
			if (side != null) {
				Vector2D v = new Vector2D.Cartesian(side.getP1(), side.getP2());
				if (v.getMagnitude() > minLength) {
					ok = true;
				}
				if (lastSide == null) {
					ok = ok && true;
				} else {
					Vector2D v2 = new Vector2D.Cartesian(lastSide.getP1(), lastSide.getP2());
					if(v2.angleBetween(v) > minAngle) {
						ok = ok && true;
					}
				}
				if(ok) {
					sides.add(side);
				}
			}
			currentX = coords[0];
			currentY = coords[1];
			lastSide = side;
		}
		return sides;
	}
}
