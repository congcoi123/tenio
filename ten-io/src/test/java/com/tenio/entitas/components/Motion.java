package com.tenio.entitas.components;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.generator.Component;

/**
 * @author Rubentxu
 */
@Component(pools = { "Test" })
public class Motion implements IComponent {
	public float x;
	public float y;

	public Motion(float x, float y) {
		this.x = x;
		this.y = y;
	}

}
