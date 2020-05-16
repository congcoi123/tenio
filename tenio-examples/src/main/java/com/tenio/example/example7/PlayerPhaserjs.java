/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.example.example7;

import com.tenio.annotation.Entity;
import com.tenio.entity.AbstractPlayer;

/**
 * The player in server
 * 
 * @author kong
 *
 */
@Entity
public final class PlayerPhaserjs extends AbstractPlayer {

	public static final int MAIN_SOCKET = 0;

	private int __x;
	private int __y;

	public PlayerPhaserjs(String name) {
		super(name);
	}

	public int getX() {
		return __x;
	}

	public int getY() {
		return __y;
	}

	public void setPosition(int x, int y) {
		__x = x;
		__y = y;
	}

}
