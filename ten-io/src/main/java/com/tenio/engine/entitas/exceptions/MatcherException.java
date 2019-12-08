package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.api.matcher.IMatcher;

/**
 * @author Rubentxu
 */
public class MatcherException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8101012713086216020L;

	public MatcherException(@SuppressWarnings("rawtypes") IMatcher matcher) {
        super("length matcher index must contain at least one, and has " + matcher.getIndices().length);
    }

}
