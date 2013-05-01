package scaatis.util;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

public class Side {
	private Point2D p1;
	private Point2D p2;
	private Shape shape;

	public Side(Shape side) {
		if (side instanceof Line2D) {
			p1 = ((Line2D) side).getP1();
			p2 = ((Line2D) side).getP2();
		} else if (side instanceof QuadCurve2D) {
			p1 = ((QuadCurve2D) side).getP1();
			p2 = ((QuadCurve2D) side).getP2();
		} else if (side instanceof CubicCurve2D) {
			p1 = ((CubicCurve2D) side).getP1();
			p2 = ((CubicCurve2D) side).getP2();
		} else {
			throw new IllegalArgumentException();
		}
		shape = side;
	}

	public Point2D getP1() {
		return p1;
	}

	public Point2D getP2() {
		return p2;
	}

	public Point2D getMidPoint() {
		if (shape instanceof CubicCurve2D) {
			CubicCurve2D curve = (CubicCurve2D) shape;
			curve.subdivide(curve, null);
			return curve.getP2();
		} else if (shape instanceof QuadCurve2D) {
			QuadCurve2D curve = (QuadCurve2D) shape;
			curve.subdivide(curve, null);
			return curve.getP2();
		} else if (shape instanceof Line2D) {
			return new Point2D.Double(
					p1.getX() + 0.5 * (p2.getX() - p1.getX()), p1.getY() + 0.5
							* (p2.getY() - p1.getY()));
		} else {
			return null;
		}
	}

	public String toString() {
		return "Side from " + p1.toString() + " to " + p2.toString();
	}

	public boolean equals(Object other) {
		if (!(other instanceof Side)) {
			return false;
		}
		return shape.equals(((Side) other).shape);
	}
}
