package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.group.Group;

/**
 * @author Rubentxu
 */
public class GroupSingleEntityException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1969433896803688676L;

	public GroupSingleEntityException(@SuppressWarnings("rawtypes") Group group) {
		super("Cannot get the single entity from " + group + "!\nGroup contains " + group.getCountEntities() + " entities:",
				"");
	}
	
}
