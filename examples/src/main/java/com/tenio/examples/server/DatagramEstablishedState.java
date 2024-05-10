package com.tenio.examples.server;

public final class DatagramEstablishedState {

  public static final byte ALLOW_TO_ACCESS = 0;
  public static final byte ESTABLISHED = 1;
  public static final byte COMMUNICATING = 2;

  private DatagramEstablishedState() {
    throw new UnsupportedOperationException();
  }
}
