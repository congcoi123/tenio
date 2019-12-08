package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.Context;
import com.tenio.engine.entitas.api.ContextInfo;

/**
 * @author Rubentxu
 */
public class ContextInfoException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3495412797235192201L;

	@SuppressWarnings("rawtypes")
	public ContextInfoException(Context pool, ContextInfo contextInfo) {
		super("Invalid ContextInfo for '" + pool + "'!\nExpected " + pool.getTotalComponents()
				+ " componentName(s) but got " + contextInfo.componentNames.length + ":", "");
	}

}
