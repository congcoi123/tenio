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

package com.tenio.common.logger;

import com.google.common.base.Throwables;
import com.tenio.common.logger.pool.StringBuilderPool;
import com.tenio.common.pool.ElementPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The recording logs of a developer is important for every system. This class
 * uses <a href="https://logging.apache.org/log4j/2.x/">Log4j</a> for robust and
 * fast logging. It uses a pool mechanism to increase performance. Every class
 * should be derived from this one so that you can have more chances to
 * investigate bugs in the production environment via daily log files.
 */
public abstract class AbstractLogger {

  /**
   * An object creation pool instance, which is used for re-using string objects.
   * see {@link ElementPool}
   */
  static final ElementPool<StringBuilder> stringPool = StringBuilderPool.getInstance();

  /**
   * A Log4j object for each logged class.
   */
  final Logger logger = LogManager.getLogger(getClass());

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param where where you put this log
   * @param tag   the tag type
   * @param msg   the message content
   */
  public void info(StringBuilder where, StringBuilder tag, StringBuilder msg) {
    if (!logger.isInfoEnabled()) {
      stringPool.repay(where);
      stringPool.repay(tag);
      stringPool.repay(msg);
      return;
    }
    var builder = stringPool.get();
    builder.append("<").append(where).append(">").append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(where);
    stringPool.repay(tag);
    stringPool.repay(msg);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param where where you put this log
   * @param tag   the tag type
   * @param msg   the message content
   */
  public void info(StringBuilder where, String tag, StringBuilder msg) {
    if (!logger.isInfoEnabled()) {
      stringPool.repay(where);
      stringPool.repay(msg);
      return;
    }
    var builder = stringPool.get();
    builder.append("<").append(where).append(">").append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(where);
    stringPool.repay(msg);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param where where you put this log
   * @param tag   the tag type
   * @param msg   the message content
   */
  public void info(StringBuilder where, StringBuilder tag, Object msg) {
    if (!logger.isInfoEnabled()) {
      stringPool.repay(where);
      stringPool.repay(tag);
      return;
    }
    var builder = stringPool.get();
    builder.append("[").append(where).append("]").append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(where);
    stringPool.repay(tag);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param where where you put this log
   * @param tag   the tag type
   * @param msg   the message content
   */
  public void info(String where, String tag, StringBuilder msg) {
    if (!logger.isInfoEnabled()) {
      stringPool.repay(msg);
      return;
    }
    var builder = stringPool.get();
    builder.append("[").append(where).append("]").append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(msg);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param where where you put this log
   * @param tag   the tag type
   * @param msg   the message content
   */
  public void info(String where, String tag, Object msg) {
    if (!logger.isInfoEnabled()) {
      return;
    }
    var builder = stringPool.get();
    builder.append("[").append(where).append("]").append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param tag the tag type
   * @param msg the message content
   */
  public void info(StringBuilder tag, StringBuilder msg) {
    if (!logger.isInfoEnabled()) {
      stringPool.repay(tag);
      stringPool.repay(msg);
      return;
    }
    var builder = stringPool.get();
    builder.append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(tag);
    stringPool.repay(msg);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param tag the tag type
   * @param msg the message content
   */
  public void info(String tag, StringBuilder msg) {
    if (!logger.isInfoEnabled()) {
      stringPool.repay(msg);
      return;
    }
    var builder = stringPool.get();
    builder.append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(msg);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>info</b> level.
   *
   * @param tag the tag type
   * @param msg the message content
   */
  public void info(String tag, Object msg) {
    if (!logger.isInfoEnabled()) {
      return;
    }
    var builder = stringPool.get();
    builder.append("[").append(tag).append("] ").append(msg);
    logger.info(builder.toString());
    stringPool.repay(builder);
  }

  /**
   * Only use for EXCEPTION detection in the server system. Be careful when using
   * it yourself. You are warned!
   *
   * @param cause the reason for this exception
   * @param extra the extra information
   */
  public void error(Throwable cause, Object... extra) {
    if (!logger.isErrorEnabled()) {
      return;
    }
    var builder = stringPool.get();
    builder.append(Throwables.getStackTraceAsString(cause));
    if (extra.length > 0) {
      builder.append("\n=========== BEGIN ERROR INFORMATION ===========\n");
      for (var e : extra) {
        builder.append(e);
      }
      builder.append("\n============ END ERROR INFORMATION ============\n");
    }
    logger.error(builder.toString());
    stringPool.repay(builder);
  }

  /**
   * Always use {@link #buildgen(Object...)} for creating {@code StringBuilder} to
   * avoid memory leak. Generate log in <b>error</b> level.
   * <br>
   * Only use for EXCEPTION detection in the server system. Be careful when using
   * it yourself. You are warned!
   *
   * @param cause the reason for this exception
   * @param msg   the extra information
   */
  public void error(Throwable cause, StringBuilder msg) {
    if (!logger.isErrorEnabled()) {
      return;
    }
    var builder = stringPool.get();
    builder.append(Throwables.getStackTraceAsString(cause));
    if (msg.length() > 0) {
      builder.append("\n=========== BEGIN ERROR INFORMATION ===========\n");
      builder.append(msg);
      builder.append("\n============ END ERROR INFORMATION ============\n");
    }
    logger.error(builder.toString());
    stringPool.repay(builder);
    stringPool.repay(msg);
  }

  /**
   * To generate {@code StringBuilder} for logging information by the
   * corresponding objects.
   *
   * @param objects the corresponding objects, see {@link Object}
   * @return an instance of the <b>StringBuilder</b>
   */
  public StringBuilder buildgen(Object... objects) {
    var builder = stringPool.get();
    for (var object : objects) {
      builder.append(object);
    }
    return builder;
  }
}
