package com.tenio.common.data.element;

import com.tenio.common.data.ZeroDataType;

public final class ZeroData {

	private final ZeroDataType __type;
	private final Object __element;

	public static ZeroData newInstance(ZeroDataType type, Object element) {
		return new ZeroData(type, element);
	}

	private ZeroData(ZeroDataType type, Object element) {
		__type = type;
		__element = element;
	}

	public ZeroDataType getType() {
		return __type;
	}

	public Object getElement() {
		return __element;
	}
}
