package scaatis.util;

public abstract class Vector2D {
	
	protected Vector2D() {
		
	}

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
	
	public String toString() {
		return "(" + getX() + ", " + getY() + ")";
	}

	public static class Polar extends Vector2D {
		private double direction;
		private double magnitude;

		public Polar(double direction, double magnitude) {
			this.direction = direction;
			this.magnitude = magnitude;
		}

		public Polar() {
			this(0, 1);
		}

		public Polar(Vector2D other) {
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
			return new Polar(new Cartesian(this).add(new Cartesian(other)));
		}

		@Override
		public Vector2D subtract(Vector2D other) {
			return add(other.scale(-1));
		}

		@Override
		public Vector2D scale(double factor) {
			return new Polar(direction, magnitude * factor);
		}

		@Override
		public Vector2D rotate(double theta) {
			return new Polar(direction + theta, magnitude);
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

	public static class Cartesian extends Vector2D {
		private double x;
		private double y;

		public Cartesian(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public Cartesian() {
			this(1, 0);
		}

		public Cartesian(Vector2D other) {
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
			return new Cartesian(x + other.getX(), y + other.getY());
		}

		@Override
		public Vector2D subtract(Vector2D other) {
			return new Cartesian(x - other.getX(), y - other.getY());
		}

		@Override
		public Vector2D scale(double factor) {
			return new Cartesian(x * factor, y * factor);
		}

		@Override
		public Vector2D rotate(double theta) {
			return new Cartesian(new Polar(this).rotate(theta));
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
