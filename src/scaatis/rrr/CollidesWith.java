package scaatis.rrr;

import java.awt.Shape;

public interface CollidesWith<T> {
	public void collideWith(T other, Shape intersection);
}
