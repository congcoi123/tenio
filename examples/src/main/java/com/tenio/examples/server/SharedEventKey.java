/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.examples.server;

public final class SharedEventKey {

  public static final String KEY_PLAYER_LOGIN = "pl";
  public static final String KEY_ALLOW_TO_ACCESS_UDP_CHANNEL = "a";
  public static final String KEY_ALLOW_TO_ACCESS_KCP_CHANNEL = "k";
  public static final String KEY_CLIENT_SERVER_ECHO = "e";
  public static final String KEY_INTEGER_ARRAY = "i";
  public static final String KEY_PLAYER_REQUEST_NEIGHBOURS = "r";
  public static final String KEY_PLAYER_GET_RESPONSE = "rr";
  public static final String KEY_COMMAND = "c";
  public static final String KEY_USER = "ku";
  public static final String KEY_DATA = "kd";
  public static final String KEY_DATA_1 = "kd1";
  public static final String KEY_DATA_2 = "kd2";
  public static final String KEY_UDP_CONVEY_ID = "u";
  public static final String KEY_UDP_MESSAGE_DATA = "d";

  private SharedEventKey() {
    throw new UnsupportedOperationException("This class does not support to create a new instance");
  }
}
