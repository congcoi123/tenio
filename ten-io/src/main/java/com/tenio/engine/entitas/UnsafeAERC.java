package com.tenio.engine.entitas;

import com.tenio.engine.entitas.api.entitas.IAERC;

/**
 * <p>
 * Automatic Entity Reference Counting (AERC) is used internally to prevent
 * <br>
 * pooling retained entities. If you use retain manually you also have to
 * <br>
 * release it manually at some point. UnsafeAERC doesn't check if the entity has
 * <br>
 * already been retained or released. It's faster, but you lose the information
 * 
 * about the owners.
 * </p>
 * 
 * @author Rubentxu
 */
public class UnsafeAERC implements IAERC {

	private int __retainCount;

	@Override
	public int retainCount() {
		return __retainCount;
	}

	@Override
	public void retain(Object owner) {
		__retainCount++;
	}

	@Override
	public void release(Object owner) {
		__retainCount--;
	}

}
