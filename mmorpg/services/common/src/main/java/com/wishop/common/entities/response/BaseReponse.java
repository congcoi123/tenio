/*
The MIT License

Copyright (c) 2019-2020 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.wishop.common.entities.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseReponse {

	public enum ResponseState {
		SUCCESS("success"), WARN("warn"), FAILED("failed");

		private String state;

		ResponseState(String state) {
			this.state = state;
		}

		public String get() {
			return state;
		}
	}

	protected Map<String, Object> body = new LinkedHashMap<>();
	protected List<String> messages;
	protected HttpStatus status;
	protected ResponseState state;

	public BaseReponse() {
		status = HttpStatus.OK;
		state = ResponseState.SUCCESS;
	}

	public BaseReponse(HttpStatus status, ResponseState state) {
		this.status = status;
		this.state = state;
	}

	public BaseReponse addMessageError(String message) {
		if (messages == null)
			messages = new ArrayList<String>();
		messages.add(message);
		return this;
	}

	public ResponseEntity<Object> get() {
		body.put("timestamp", new Date());
		body.put("status", status.value());
		body.put("state", state.get());
		return new ResponseEntity<Object>(body, status);
	}

}
