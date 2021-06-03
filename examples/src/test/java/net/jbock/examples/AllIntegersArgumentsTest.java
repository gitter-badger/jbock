package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class AllIntegersArgumentsTest {

  private final ParserTestFixture<AllIntegersArguments> f =
      ParserTestFixture.create(new AllIntegersArgumentsParser());

  @Test
  void listOfInteger() {
    f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1").succeeds(
        "positional", emptyList(),
        "listOfIntegers", asList(1, 2, 2, 3),
        "optionalInteger", Optional.empty(),
        "integer", 1,
        "primitiveInt", 1);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1", "--obj=1", "--prim=1").succeeds(
        "positional", emptyList(),
        "listOfIntegers", emptyList(),
        "optionalInteger", Optional.of(1),
        "integer", 1,
        "primitiveInt", 1);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1", "--prim=1", "5", "3", "--opti=5").succeeds(
        "positional", asList(5, 3),
        "listOfIntegers", emptyList(),
        "optionalInteger", Optional.empty(),
        "integer", 1,
        "optionalInt", OptionalInt.of(5),
        "primitiveInt", 1);
  }
}
