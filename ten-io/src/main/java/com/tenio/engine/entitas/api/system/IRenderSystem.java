package com.tenio.engine.entitas.api.system;

import com.tenio.engine.physic.graphic.Paint;

/**
 * <p>
 * Display simple shapes for debugging 
 * </p>
 * 
 * @author Rubentxu
 */
public interface IRenderSystem extends ISystem {

	void render(Paint paint);

}
