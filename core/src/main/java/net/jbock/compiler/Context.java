package net.jbock.compiler;

import net.jbock.CommandLineArguments;
import net.jbock.com.squareup.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.Util.asType;

final class Context {

  // the annotated class
  final TypeElement sourceType;

  // the *_Parser class that will be generated
  final ClassName generatedClass;

  // corresponds to _all_ abstract methods of the source type (in source order, inheritance not considered)
  final List<Param> parameters;

  // only the methods that have the Positional annotation (in source order, inheritance not considered)
  private final List<Param> positionalParameters;

  // should "--" end option parsing
  private final boolean allowEscape;

  // should unknown parameters that start with dash be forbidden
  final boolean strict;

  // true if upper-casing the method names would cause a naming conflict
  final boolean problematicOptionNames;

  // true if the source type does not already define toString
  final boolean generateToString;

  // true if --help is a special token
  final boolean addHelp;

  // a set of only the non-positional param types in the sourceType
  final Set<Type> paramTypes;

  // a set of only the positional param types in the sourceType
  final Set<PositionalType> positionalParamTypes;

  // general usage information
  final List<String> overview;

  // general usage information
  final String programName;

  // general usage information
  final String missionStatement;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      List<Param> positionalParameters,
      boolean allowEscape,
      boolean problematicOptionNames,
      boolean strict,
      boolean generateToString,
      boolean addHelp,
      Set<Type> paramTypes,
      Set<PositionalType> positionalParamTypes,
      List<String> overview,
      String programName,
      String missionStatement) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.positionalParameters = positionalParameters;
    this.allowEscape = allowEscape;
    this.problematicOptionNames = problematicOptionNames;
    this.strict = strict;
    this.generateToString = generateToString;
    this.addHelp = addHelp;
    this.paramTypes = paramTypes;
    this.positionalParamTypes = positionalParamTypes;
    this.overview = overview;
    this.programName = programName;
    this.missionStatement = missionStatement;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      Set<Type> paramTypes,
      Set<PositionalType> positionalParamTypes) {
    ClassName generatedClass = parserClass(ClassName.get(asType(sourceType)));
    boolean problematicOptionNames = problematicOptionNames(parameters);
    boolean allowEscape = sourceType.getAnnotation(CommandLineArguments.class).allowEscape();
    List<Param> positionalParameters = parameters.stream().filter(p -> p.positionalType != null).collect(toList());
    boolean strict = sourceType.getAnnotation(CommandLineArguments.class).strict();
    boolean addHelp = sourceType.getAnnotation(CommandLineArguments.class).addHelp();
    boolean generateToString = methodsIn(sourceType.getEnclosedElements()).stream()
        .filter(method -> method.getParameters().isEmpty())
        .map(ExecutableElement::getSimpleName)
        .map(Name::toString)
        .noneMatch(s -> s.equals("toString"));
    List<String> description = Arrays.asList(sourceType.getAnnotation(CommandLineArguments.class).overview());
    String missionStatement = sourceType.getAnnotation(CommandLineArguments.class).missionStatement();
    return new Context(
        sourceType,
        generatedClass,
        parameters,
        positionalParameters,
        allowEscape,
        problematicOptionNames,
        strict,
        generateToString,
        addHelp,
        paramTypes,
        positionalParamTypes,
        description,
        programName(sourceType),
        missionStatement);
  }

  private static boolean problematicOptionNames(List<Param> parameters) {
    Set<String> uppercaseArgumentNames = parameters.stream()
        .map(Param::methodName)
        .map(Util::snakeCase)
        .collect(Collectors.toSet());
    return uppercaseArgumentNames.size() < parameters.size();
  }

  private static ClassName parserClass(ClassName type) {
    String name = String.join("_", type.simpleNames()) + "_Parser";
    return type.topLevelClassName().peerClass(name);
  }

  private static String programName(TypeElement sourceType) {
    CommandLineArguments annotation = sourceType.getAnnotation(CommandLineArguments.class);
    if (!annotation.programName().isEmpty()) {
      return annotation.programName();
    }
    switch (sourceType.getNestingKind()) {
      case MEMBER:
        return Util.asType(sourceType.getEnclosingElement()).getSimpleName().toString();
      default:
        return sourceType.getSimpleName().toString();
    }
  }

  /**
   * @param j must be the Option index of a positional param
   * @return the index in the list of all positional parameters, of the param that's specified by {@code j}.
   */
  int positionalIndex(int j) {
    Param param = parameters.get(j);
    for (int i = 0; i < positionalParameters.size(); i++) {
      Param p = positionalParameters.get(i);
      if (p.index == param.index) {
        return i;
      }
    }
    // j is not the Option index of a positional param.
    // Calling this method with such an argument is not allowed.
    throw new IllegalArgumentException(
        "Not a positional parameter: " + j);
  }

  /**
   * Determine how many positional arguments the user can specify at most,
   * before doubledash.
   *
   * @return the maximum number of positional arguments,
   * or {@code OptionalInt.empty()} if there is no limit
   */
  OptionalInt maxPositional() {
    if (positionalParameters.isEmpty()) {
      return OptionalInt.empty();
    }
    if (positionalParamTypes.contains(PositionalType.POSITIONAL_LIST)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(positionalParameters.size());
  }

  boolean hasPositional() {
    return !positionalParameters.isEmpty();
  }

  boolean allowEscape() {
    return allowEscape && positionalParamTypes.contains(PositionalType.POSITIONAL_LIST);
  }
}
