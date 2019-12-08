package com.tenio.engine.entitas.api;

import java.util.Arrays;

/**
 * @author Rubentxu
 */
public class ContextInfo {

	public String contextName;
	public String[] componentNames;
	@SuppressWarnings("rawtypes")
	public Class[] componentTypes;

	public ContextInfo(String contextName, String[] componentNames, @SuppressWarnings("rawtypes") Class[] componentTypes) {
		this.contextName = contextName;
		this.componentNames = componentNames;
		this.componentTypes = componentTypes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ContextInfo)) {
			return false;
		}

		ContextInfo that = (ContextInfo) o;

		if (contextName != null ? !contextName.equals(that.contextName) : that.contextName != null) {
			return false;
		}
		// Comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(componentNames, that.componentNames)) {
			return false;
		}
		// Comparing Object[] arrays with Arrays.equals
		return Arrays.equals(componentTypes, that.componentTypes);
	}

	@Override
	public int hashCode() {
		int result = contextName != null ? contextName.hashCode() : 0;
		result = 31 * result + Arrays.hashCode(componentNames);
		result = 31 * result + Arrays.hashCode(componentTypes);
		return result;
	}

	@Override
	public String toString() {
		return "ContextInfo{" + "contextName='" + contextName + '\'' + ", componentNames="
				+ Arrays.toString(componentNames) + '}';
	}
	
}
