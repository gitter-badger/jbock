package net.jbock.coerce.either;

public class Left<A, B> extends Either<A, B> {

  private final A value;

  public Left(A a) {
    value = a;
  }

  public A value() {
    return value;
  }
}
