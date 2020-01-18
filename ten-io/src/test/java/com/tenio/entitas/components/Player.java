package com.tenio.entitas.components;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.generator.Component;

/**
 * @author Rubentxu
 */
@Component(pools = { "Test", "Test2" }, isSingleEntity = true)
public class Player implements IComponent {
	
}
