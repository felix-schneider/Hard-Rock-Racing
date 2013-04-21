package scaatis.rrr.event;

import java.util.EventListener;

public interface CheckPointListener extends EventListener {
	public void checkPoint(CheckPointEvent e);
}
