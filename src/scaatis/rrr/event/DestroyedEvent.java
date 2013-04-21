package scaatis.rrr.event;

import java.util.EventObject;

public class DestroyedEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	public DestroyedEvent(Object source) {
		super(source);
	}

}
