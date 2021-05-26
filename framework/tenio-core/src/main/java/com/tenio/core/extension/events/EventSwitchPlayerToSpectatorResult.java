package com.tenio.core.extension.events;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.defines.results.SwitchedPlayerSpectatorResult;

public interface EventSwitchPlayerToSpectatorResult {

	void handle(Player player, SwitchedPlayerSpectatorResult result);

}
