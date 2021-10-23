/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.core.monitoring.system;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.monitoring.define.SystemInfoType;
import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;

/**
 * For logging the OS system information.
 */
public final class SystemInfo extends SystemLogger {

  /**
   * Logging the system information.
   */
  public void logSystemInfo() {
    var logger = buildgen("\n");
    var runtime = Runtime.getRuntime();

    logger.append("\t").append("Processor(s): ").append(runtime.availableProcessors()).append("\n");
    logger.append("\t").append("VM Max. memory: ").append(runtime.maxMemory() / 1000000L)
        .append(" MB")
        .append("\n");

    for (var prop : SystemInfoType.values()) {
      logger.append("\t").append(prop.toString()).append(": ")
          .append(System.getProperty(prop.getValue()))
          .append("\n");
    }

    logger.append("\t").append("Default charset: " + Charset.defaultCharset()).append("\n");

    info("SYSTEM INFORMATION", logger);
  }

  /**
   * Logging the net card information.
   */
  public void logNetCardsInfo() {
    var logger = buildgen("\n");

    try {
      var list = NetworkInterface.getNetworkInterfaces();

      while (list.hasMoreElements()) {
        var networkInterface = list.nextElement();
        logger.append("\t").append("Card: ").append(networkInterface.getDisplayName()).append("\n");

        var addresses = networkInterface.getInetAddresses();

        while (addresses.hasMoreElements()) {
          var inetAddress = addresses.nextElement();
          logger.append("\t").append("\t").append(" ->").append(inetAddress.getHostAddress())
              .append("\n");
        }
      }

      info("NETWORK INFORMATION", logger);

    } catch (SocketException e) {
      error(e, "Exception while discovering network cards");
    }
  }

  /**
   * Logging the disk information.
   */
  public void logDiskInfo() {
    var logger = buildgen("\n");

    /* Get a list of all file system roots on this system */
    var roots = File.listRoots();

    /* For each file system root, print some info */
    for (File root : roots) {
      logger.append("\t").append("File system root: ").append(root.getAbsolutePath()).append("\n");
      logger.append("\t").append("Total space (bytes): ").append(root.getTotalSpace()).append("\n");
      logger.append("\t").append("Free space (bytes): ").append(root.getFreeSpace()).append("\n");
      logger.append("\t").append("Usable space (bytes): ").append(root.getUsableSpace())
          .append("\n");
    }

    info("DISK INFORMATION", logger);
  }
}
