/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.examples.example4.handlers;

import java.util.ArrayList;
import java.util.List;

import com.tenio.common.bootstrap.annotations.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotations.Component;
import com.tenio.common.configuration.Configuration;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventServerInitialization;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.examples.example4.constant.Example4Constant;
import com.tenio.examples.example4.entities.Vehicle;
import com.tenio.examples.example4.world.World;
import com.tenio.examples.example4.world.WorldListener;
import com.tenio.examples.server.SharedEventKey;

@Component
public final class ServerInitializedHandler extends AbstractExtension implements EventServerInitialization {

	@AutowiredAcceptNull
	private HeartBeatManager __heartbeatManager;

	@Override
	public void handle(String serverName, Configuration configuration) {

		var world = new World(Example4Constant.DESIGN_WIDTH, Example4Constant.DESIGN_HEIGHT);
		world.debug("[TenIO] Server Debugger : Stress Movement Simulation");
		world.setListener(new WorldListener() {

			@Override
			public void updateVehiclePosition(Vehicle vehicle) {
				var players = api().getAllPlayers();

				var data = object();
				var array = new ArrayList<Integer>();
				array.add(vehicle.getIndex());
				array.add((int) vehicle.getPositionX());
				array.add((int) vehicle.getPositionY());
				array.add((int) vehicle.getRotation());
				data.putIntegerArray(SharedEventKey.KEY_PLAYER_GET_RESPONSE, array);

				response().setRecipients(players).setContent(data.toBinary()).prioritizedUdp().write();
			}

			@Override
			public void reponseVehicleNeighbours(String playerName, List<Vehicle> neighbours, int currentFps) {
				var player = api().getPlayerByName(playerName);
				if (player != null) {
					var data = object();
					data.putInteger(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS, currentFps);

					response().setRecipient(player).setContent(data.toBinary()).write();
				}
			}

			@Override
			public int getCcu() {
				return api().getPlayerCount();
			}
		});

		try {
			__heartbeatManager.initialize(1);
			__heartbeatManager.create("world", world);
		} catch (Exception e) {
			error(e, "world");
		}
	}

}
