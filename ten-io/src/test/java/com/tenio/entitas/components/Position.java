package com.tenio.entitas.components;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.generator.Component;

/**
 * @author Rubentxu
 */
@Component(pools = { "Test" })
public class Position implements IComponent {
	public float x, y;

	public Position() {
		this.x = 1;
		this.y = 1;
	}

	public Position(float x, float y) {
		this.x = x;
		this.y = y;
	}

}
