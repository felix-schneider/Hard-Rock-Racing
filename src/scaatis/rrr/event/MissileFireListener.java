package scaatis.rrr.event;

import java.util.EventListener;

public interface MissileFireListener extends EventListener {
	public void missileFired(MissileFireEvent e);
}
