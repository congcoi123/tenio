package com.tenio.engine.entitas.caching;

/**
 * @author kong
 * 
 * Nov 4, 2019
 */
public interface Factory<T> {
	
	T create();
	
}