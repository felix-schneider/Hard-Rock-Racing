package scaatis.rrr;

public enum Direction {
	LEFT, UP, RIGHT, DOWN;

	public Direction opposite() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default:
			return null;
		}
	}

	public Direction clockwise() {
		switch (this) {
		case LEFT:
			return UP;
		case UP:
			return RIGHT;
		case RIGHT:
			return DOWN;
		case DOWN:
			return LEFT;
		default:
			return null;
		}
	}

	public Direction cclockwise() {
		switch (this) {
		case LEFT:
			return DOWN;
		case DOWN:
			return RIGHT;
		case RIGHT:
			return UP;
		case UP:
			return LEFT;
		default:
			return null;
		}
	}
}
