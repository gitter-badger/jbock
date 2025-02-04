package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class ComplicatedMapperArguments {

  @Option(
      names = "--number",
      converter = MyConverter.class)
  abstract Integer number();

  @Option(
      names = "--numbers",
      converter = LazyNumberConverter.class)
  abstract List<LazyNumber> numbers();

  @Converter
  static class LazyNumberConverter implements Supplier<StringConverter<LazyNumber>> {
    @Override
    public StringConverter<LazyNumber> get() {
      return StringConverter.create(s -> () -> Integer.valueOf(s));
    }
  }

  interface LazyNumber extends Supplier<Integer> {
  }

  @Converter
  static class MyConverter implements Supplier<StringConverter<Integer>> {
    @Override
    public StringConverter<Integer> get() {
      return StringConverter.create(new Zapper());
    }
  }

  static class Zapper implements Foo<String> {
    public Integer apply(String s) {
      return 1;
    }
  }

  interface Xi<A, T, B> extends Function<B, A> {
  }

  interface Zap<T, B, A> extends Xi<A, T, B> {
  }

  interface Foo<X> extends Zap<X, String, Integer> {
  }
}
