package com.tenio.entitas.components;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.generator.Component;

/**
 * @author Rubentxu
 */
@Component(pools = { "Test" })
public class Score implements IComponent {
	public String text;
	public int x;
	public int y;

	public Score(String text, int x, int y) {

		this.text = text;
		this.x = x;
		this.y = y;

	}

}
