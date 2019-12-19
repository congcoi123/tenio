package com.tenio.event;

import com.tenio.event.logic.LEventManager;
import com.tenio.event.main.TEventManager;
import com.tenio.logger.AbstractLogger;

public final class EventManager extends AbstractLogger {

	private static TEventManager __tEvent = new TEventManager();
	private static LEventManager __lEvent = new LEventManager();

	public static TEventManager getEvent() {
		return __tEvent;
	}
	
	public static LEventManager getLogic() {
		return __lEvent;
	}
	
}
