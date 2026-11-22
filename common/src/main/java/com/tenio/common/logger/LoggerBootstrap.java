/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.common.logger;

import org.apache.logging.log4j.core.config.Configurator;

import java.net.URL;

/**
 * Logger Bootstrapper.
 *
 * @since 0.6.10
 */
public final class LoggerBootstrap {

    /**
     * Initializes configuration for Log4j2.
     *
     * @param fwLog4j2File   the default configuration file which should be used when the user self-defined
     *                       configuration file not found
     * @param userLog4j2File the user self-defined configuration file
     */
    public static void initialize(String fwLog4j2File, String userLog4j2File) {
        // 1. Respect system property (highest priority)
        if (System.getProperty("log4j.configurationFile") != null) {
            System.out.println("[LOG4J2] Using user configuration: " + userLog4j2File);
            return;
        }

        // 2. Check if application provides its own config
        if (hasUserLog4jConfig(userLog4j2File)) {
            System.out.println("[LOG4J2] Using user configuration: " + userLog4j2File);
            return;
        }

        // 3. Fallback to framework default
        URL url = LoggerBootstrap.class
                .getClassLoader()
                .getResource(fwLog4j2File);

        if (url != null) {
            Configurator.initialize(null, url.toString());
            System.out.println("[LOG4J2] Using framework default configuration: " + url);
        } else {
            System.err.println("[LOG4J2] No default configuration found!");
        }
    }

    private static boolean hasUserLog4jConfig(String userLog4j2File) {
        try {
            return Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(userLog4j2File)
                    .hasMoreElements();
        } catch (Exception e) {
            return false;
        }
    }
}
