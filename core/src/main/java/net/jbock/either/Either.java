package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return Left.create(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return Right.create(value);
  }

  public static <L, R> Either<L, R> fromOptionalSuccess(Supplier<? extends L> failure, Optional<? extends R> maybeSuccess) {
    return maybeSuccess.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(failure.get()));
  }

  public static <L, R> Either<L, R> fromOptionalFailure(Supplier<? extends R> success, Optional<? extends L> maybeFailure) {
    return maybeFailure.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(success.get()));
  }

  public abstract boolean isPresent();

  public abstract <R2> Either<L, R2> map(Function<? super R, ? extends R2> rightMapper);

  public abstract <R2> Either<L, R2> select(Function<? super R, ? extends Either<? extends L, ? extends R2>> choice);

  public abstract Either<L, R> filter(Function<? super R, ? extends Optional<? extends L>> fail);

  public final Either<L, R> filter(Supplier<? extends Optional<? extends L>> fail) {
    return filter(r -> fail.get());
  }

  public final <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> leftMapper) {
    @SuppressWarnings("unchecked")
    Either<L2, R> result = (Either<L2, R>) swap().map(leftMapper).swap();
    return result;
  }

  public final <L2> Either<L2, R> selectLeft(Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper) {
    @SuppressWarnings("unchecked")
    Either<L2, R> result = (Either<L2, R>) swap().select(l -> leftMapper.apply(l).swap()).swap();
    return result;
  }

  public final Either<L, R> maybeRecover(Function<? super L, ? extends Optional<? extends R>> choice) {
    return swap().filter(choice).swap();
  }

  public final Either<L, R> maybeRecover(Supplier<? extends Optional<? extends R>> choice) {
    return maybeRecover(value -> choice.get());
  }

  public abstract R orRecover(Function<? super L, ? extends R> recover);

  public final R orRecover(Supplier<? extends R> success) {
    return orRecover(left -> success.get());
  }

  public abstract Either<R, L> swap();

  public final void ifPresent(Consumer<? super R> rightConsumer) {
    ifPresentOrElse(rightConsumer, l -> {
    });
  }

  public abstract void ifPresentOrElse(Consumer<? super R> rightConsumer, Consumer<? super L> leftConsumer);

  public abstract <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> leftMapper) throws X;
}
