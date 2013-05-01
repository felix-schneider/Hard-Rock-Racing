package scaatis.rrr.event;

import java.util.EventObject;

public class LapCompletedEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public LapCompletedEvent(Object source) {
		super(source);
	}
	
}
