package scaatis.rrr.event;

import java.util.EventListener;

public interface MineDropListener extends EventListener {
	public void mineDropped(MineDropEvent event);
}
