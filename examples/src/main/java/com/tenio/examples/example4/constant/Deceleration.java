package com.tenio.examples.example4.constant;

public enum Deceleration {

  SLOW(3),
  NORMAL(2),
  FAST(1);

  private final int value;

  Deceleration(final int value) {
    this.value = value;
  }

  public int get() {
    return value;
  }

  @Override
  public String toString() {
    return name();
  }
}
