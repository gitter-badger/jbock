package net.jbock.coerce.either;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Left<L, R> extends Either<L, R> {

  private final L left;

  Left(L left) {
    this.left = left;
  }

  @SuppressWarnings("unchecked")
  private <R2> Left<L, R2> createLeft(L newValue) {
    if (newValue == left) {
      return (Left<L, R2>) this;
    }
    return new Left<>(newValue);
  }

  public L value() {
    return left;
  }

  @Override
  public boolean isRight() {
    return false;
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightMapper) {
    return createLeft(left);
  }

  @Override
  public Either<L, Void> accept(Consumer<R> rightConsumer) {
    return createLeft(left);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper) {
    return createLeft(left);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper) {
    return createLeft(left);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    throw leftMapper.apply(left);
  }
}
