package com.tenio.examples.server;

public final class UdpEstablishedState {

  public static final byte ALLOW_TO_ACCESS = 0;
  public static final byte ESTABLISHED = 1;
  public static final byte COMMUNICATING = 2;

  private UdpEstablishedState() {
    throw new UnsupportedOperationException();
  }
}
