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

import org.springframework.http.ResponseEntity;

public class PageResultsResponse extends ListResultsResponse {

	private int page;
	private int limit;
	private int pageSize;
	private long count;

	public PageResultsResponse(int page, int limit, int pageSize, long count) {
		super();
		this.page = page;
		this.limit = limit;
		this.pageSize = pageSize;
		this.count = count;
	}

	@Override
	public ResponseEntity<Object> get() {
		body.put("current_page", page);
		body.put("page_size", pageSize);
		body.put("pages", (int) Math.ceil((count * 1.0) / limit));
		body.put("items", count);
		return super.get();
	}
	
}
