package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.ProcessorTest.fromSource;

class ConverterTest {

  @Test
  void converterImplementsBothFunctionAndSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Converter",
        "class MapMap extends StringConverter<String> implements Supplier<StringConverter<String>> {",
        "  public String convert(String token) { return null; }",
        "  public StringConverter<String> get() { return null; }",
        "}",
        "",
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract String foo();",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter must extend StringConverter<X> or implement Supplier<StringConverter<X>> but not both");
  }

  @Test
  void converterDoesNotImplementFunction() {
    JavaFileObject javaFile = fromSource(
        "@Converter",
        "class MapMap {}",
        "",
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract String foo();",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter must extend StringConverter<X> or implement Supplier<StringConverter<X>>");
  }

  @Test
  void missingConverterAnnotation() {
    JavaFileObject javaFile = fromSource(
        "class MapMap implements Function<String, String> {",
        "  public String apply(String s) { return null; }",
        "}",
        "",
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract String foo();",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter must be an inner class of the command class, " +
            "or carry the @Converter annotation");
  }

  @Test
  void validArrayMapperSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = ArrayMapper.class)",
        "  abstract Optional<int[]> foo();",
        "",
        "  @Converter",
        "  static class ArrayMapper implements Supplier<StringConverter<int[]>> {",
        "    public StringConverter<int[]> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void validArrayMapper() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = ArrayMapper.class)",
        "  abstract Optional<int[]> foo();",
        "",
        "  static class ArrayMapper extends StringConverter<int[]> {",
        "    public int[] convert(String s) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void validBooleanList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameters(converter = BooleanMapper.class)",
        "  abstract List<Boolean> booleanList();",
        "",
        "  @Converter",
        "  static class BooleanMapper implements Supplier<StringConverter<Boolean>> {",
        "    public StringConverter<Boolean> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void parametersInvalidNotList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameters(converter = MyConverter.class)",
        "  abstract Integer something();",
        "",
        "  @Converter",
        "  static class MyConverter extends StringConverter<Integer> {",
        "    public Integer convert(String token) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("use @Parameter here");
  }

  @Test
  void parametersInvalidNotListOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameters(converter = MyConverter.class)",
        "  abstract Optional<Integer> something();",
        "",
        "  @Converter",
        "  static class MyConverter extends StringConverter<Integer> {",
        "    public Integer convert(String token) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("use @Parameter here");
  }

  @Test
  void parameterInvalidList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0, converter = MyConverter.class)",
        "  abstract List<Integer> something();",
        "",
        "  @Converter",
        "  static class MyConverter extends StringConverter<Integer> {",
        "    public Integer convert(String token) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("use @Parameters here");
  }

  @Test
  void invalidBounds() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 1, converter = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  @Converter",
        "  static class BoundMapper<E extends Integer> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter");
  }

  @Test
  void indirectSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 1, converter = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  @Converter",
        "  static class BoundMapper implements Katz<String> {",
        "    public StringConverter<String> get() { return null; }",
        "  }",
        "",
        "  interface Katz<T> extends Supplier<StringConverter<T>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter must extend StringConverter<X> or implement Supplier<StringConverter<X>>");
  }

  @Test
  void converterInvalidPrivateConstructor() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "",
        "    private MapMap() {}",
        "",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("constructor");
  }

  @Test
  void converterInvalidNoDefaultConstructor() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "",
        "    MapMap(int i) {}",
        "",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("default constructor not found");
  }

  @Test
  void converterInvalidConstructorException() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "",
        "    MapMap() throws java.io.IOException {}",
        "",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("default constructor not found");
  }

  @Test
  void converterInvalidNonstaticInnerClass() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  class MapMap implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("nested class must be static");
  }

  @Test
  void converterInvalidReturnsString() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<String>> {",
        "    public StringConverter<String> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter should extend StringConverter<Integer>");
  }

  @Test
  void converterInvalidReturnsStringOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract java.util.OptionalInt number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<String>> {",
        "    public StringConverter<String> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter should extend StringConverter<Integer>");
  }

  @Test
  void converterInvalidReturnsStringList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract List<Integer> number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<String>> {",
        "    public StringConverter<String> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("converter should extend StringConverter<Integer>");
  }

  @Test
  void converterValidTypevars() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Supplier<String> string();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Supplier<String>>> {",
        "    public StringConverter<Supplier<String>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterValidNestedTypevars() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Supplier<Optional<String>> string();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Supplier<Optional<String>>>> {",
        "    public StringConverter<Supplier<Optional<String>>> get() { return null; }",
        "  }",
        "}");

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterInvalidRawFunctionSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter> {",
        "    public StringConverter get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("raw type in converter class");
  }

  @Test
  void converterInvalidRawSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier {",
        "    public Object get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("raw type in converter class");
  }

  @Test
  void converterInvalidRawFunction() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Converter",
        "  static class MapMap extends StringConverter {",
        "    public Object convert(String token) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("raw type in converter class");
  }

  @Test
  void converterValid() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract List<java.util.OptionalInt> numbers();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<java.util.OptionalInt>> {",
        "    public StringConverter<java.util.OptionalInt> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterValidBytePrimitive() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract byte number();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Byte>> {",
        "    public StringConverter<Byte> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterValidOptionalInteger() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Optional<Integer> number();",
        "",
        "  @Converter",
        "  static class MapMap extends StringConverter<Integer> {",
        "    public Integer convert(String token) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void implicitMapperOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract java.util.OptionalInt b();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Integer>> {",
        "    public StringConverter<Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract java.util.OptionalInt b();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<java.util.OptionalInt>> {",
        "    public StringConverter<java.util.OptionalInt> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterOptionalInteger() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract Optional<Integer> b();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Optional<Integer>>> {",
        "    public StringConverter<Optional<Integer>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void oneOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract java.util.OptionalInt b();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Integer>> {",
        "    public StringConverter<Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void converterValidListOfSet() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\", converter = MapMap.class)",
        "  abstract List<Set<Integer>> sets();",
        "",
        "  @Converter",
        "  static class MapMap implements Supplier<StringConverter<Set<Integer>>> {",
        "    public StringConverter<Set<Integer>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }
}
