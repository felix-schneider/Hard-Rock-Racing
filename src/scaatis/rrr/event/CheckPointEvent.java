package scaatis.rrr.event;

import java.util.EventObject;

import scaatis.rrr.tracktiles.CheckPoint;

public class CheckPointEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private CheckPoint checkPoint;

	public CheckPointEvent(Object source, CheckPoint checkPoint) {
		super(source);
		this.checkPoint = checkPoint;
	}

	public CheckPoint getCheckPoint() {
		return checkPoint;
	}
}
