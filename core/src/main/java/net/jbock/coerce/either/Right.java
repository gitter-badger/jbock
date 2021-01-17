package net.jbock.coerce.either;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Right<L, R> extends Either<L, R> {

  private static final Either<?, Void> CONTAINS_NULL = new Right<>(null);

  @SuppressWarnings("unchecked")
  static <__IGNORE> Either<__IGNORE, Void> containsNull() {
    return (Right<__IGNORE, Void>) CONTAINS_NULL;
  }

  private final R right;

  Right(R right) {
    this.right = right;
  }

  public R value() {
    return right;
  }

  @Override
  public boolean isRight() {
    return true;
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightMapper) {
    return right(rightMapper.apply(right));
  }

  @Override
  public Either<L, Void> accept(Consumer<R> rightConsumer) {
    rightConsumer.accept(value());
    return right();
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper) {
    return rightMapper.apply(right);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper) {
    return rightMapper.get();
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    return right;
  }
}
