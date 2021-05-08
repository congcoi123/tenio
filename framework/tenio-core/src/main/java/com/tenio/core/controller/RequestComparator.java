package com.tenio.core.controller;

import java.util.Comparator;

import com.tenio.core.network.entity.protocol.Request;

public final class RequestComparator implements Comparator<Request> {

	@Override
	public int compare(Request request1, Request request2) {
		int result = 0;

		if (request1.getPriority().getValue() < request2.getPriority().getValue()) {
			result = -1;
		} else if (request1.getPriority() == request2.getPriority()) {
			if (request1.getTimeStamp() < request2.getTimeStamp()) {
				result = -1;
			} else if (request1.getTimeStamp() > request2.getTimeStamp()) {
				result = 1;
			} else {
				result = 0;
			}
		} else {
			result = 1;
		}

		return result;
	}

}
