package scaatis.util;

public abstract class Vector2D {

	public abstract double getX();

	public abstract double getY();

	public abstract Vector2D add(Vector2D other);

	public abstract Vector2D subtract(Vector2D other);

	public abstract Vector2D scale(double factor);

	public abstract Vector2D rotate(double theta);

	public Vector2D normalize() {
		return scale(1 / getMagnitude());
	}

	public abstract double dotProduct(Vector2D other);

	public double angleBetween(Vector2D other) {
		return other.getDirection() - getDirection();
	}

	public abstract double getDirection();

	public abstract double getMagnitude();

	public abstract double getSquareMagnitude();

	public final class DirMag extends Vector2D {
		private double direction;
		private double magnitude;

		public DirMag(double direction, double magnitude) {
			this.direction = direction;
			this.magnitude = magnitude;
		}

		public DirMag() {
			this(0, 1);
		}

		public DirMag(Vector2D other) {
			this(other.getDirection(), other.getMagnitude());
		}

		@Override
		public double getX() {
			return Math.cos(direction) * magnitude;
		}

		@Override
		public double getY() {
			return Math.sin(direction) * magnitude;
		}

		@Override
		public Vector2D add(Vector2D other) {
			return new DirMag(new XY(this).add(new XY(other)));
		}

		@Override
		public Vector2D subtract(Vector2D other) {
			return add(other.scale(-1));
		}

		@Override
		public Vector2D scale(double factor) {
			return new DirMag(direction, magnitude * factor);
		}

		@Override
		public Vector2D rotate(double theta) {
			return new DirMag(direction + theta, magnitude);
		}

		@Override
		public double dotProduct(Vector2D other) {
			return getMagnitude() * other.getMagnitude()
					* Math.cos(angleBetween(other));
		}

		@Override
		public double getDirection() {
			return direction;
		}

		@Override
		public double getMagnitude() {
			return magnitude;
		}

		@Override
		public double getSquareMagnitude() {
			return magnitude * magnitude;
		}
	}

	public final class XY extends Vector2D {
		private double x;
		private double y;

		public XY(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public XY() {
			this(1, 0);
		}

		public XY(Vector2D other) {
			this(other.getX(), other.getY());
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

		@Override
		public Vector2D add(Vector2D other) {
			return new XY(x + other.getX(), y + other.getY());
		}

		@Override
		public Vector2D subtract(Vector2D other) {
			return new XY(x - other.getX(), y - other.getY());
		}

		@Override
		public Vector2D scale(double factor) {
			return new XY(x * factor, y * factor);
		}

		@Override
		public Vector2D rotate(double theta) {
			return new XY(new DirMag(this).rotate(theta));
		}

		@Override
		public double dotProduct(Vector2D other) {
			return x * other.getX() + y * other.getY();
		}

		@Override
		public double getDirection() {
			return Math.atan2(y, x);
		}

		@Override
		public double getMagnitude() {
			return Math.sqrt(x * x + y * y);
		}

		@Override
		public double getSquareMagnitude() {
			return x * x + y * y;
		}
	}
}
