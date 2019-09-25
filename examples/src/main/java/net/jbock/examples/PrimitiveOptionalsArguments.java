package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class PrimitiveOptionalsArguments {

  @Parameter(shortName = 'I', mappedBy = IntegerMapper.class)
  abstract OptionalInt simpleInt();

  @Parameter(shortName = 'L', mappedBy = LongMapper.class)
  abstract OptionalLong simpleLong();

  @Parameter(shortName = 'D', mappedBy = DoubleMapper.class)
  abstract OptionalDouble simpleDouble();

  @Parameter(shortName = 'i', mappedBy = IntegerMapper.class)
  abstract OptionalInt mappedInt();

  @Parameter(shortName = 'l', mappedBy = LongMapper.class)
  abstract OptionalLong mappedLong();

  @Parameter(shortName = 'd', mappedBy = DoubleMapper.class)
  abstract OptionalDouble mappedDouble();

  static class IntegerMapper implements Supplier<Function<String, Integer>> {
    @Override
    public Function<String, Integer> get() {
      return Integer::valueOf;
    }
  }

  static class LongMapper implements Supplier<Function<String, Long>> {
    @Override
    public Function<String, Long> get() {
      return Long::valueOf;
    }
  }

  static class DoubleMapper implements Supplier<Function<String, Double>> {
    @Override
    public Function<String, Double> get() {
      return Double::valueOf;
    }
  }
}
