/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.examples.example8;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class shows how to send a HTTP requests to server.
 */
public final class TestClientRestful {

  public TestClientRestful() {
    System.out.println(executeRequest("http://localhost:9999/ping", "param=test", "GET"));
    System.err.println(executeRequest("http://localhost:9999/delete", "param=test", "DELETE"));
    System.out.println(executeRequest("http://localhost:9999/change", "param=test", "PUT"));
  }

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    new TestClientRestful();
  }

  private String executeRequest(String targetURL, String urlParameters, String method) {
    HttpURLConnection connection = null;

    try {
      // Create connection
      var url = new URL(targetURL);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(method);
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      connection.setRequestProperty("Content-Length",
          Integer.toString(urlParameters.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");

      connection.setUseCaches(false);
      connection.setDoOutput(true);

      // Send request
      var wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.close();

      // Get Response
      var is = connection.getInputStream();
      var rd = new BufferedReader(new InputStreamReader(is));
      var response = new StringBuilder(); // or StringBuffer if Java version 5+
      String line;
      while ((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
