package com.tenio.entitas.components;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.generator.Component;

/**
 * @author Rubentxu
 */
@Component(pools = { "Test" })
public class View implements IComponent {
	public int shape;

	public View(int shape) {
		this.shape = shape;
	}
}
