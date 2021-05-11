package net.jbock.convert.matching.auto;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.convert.AutoMapper;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.convert.matching.ConverterFinder;
import net.jbock.convert.matching.Match;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.STRING;
import static net.jbock.either.Either.left;

public class AutoConverterFinder extends ConverterFinder {

  private static final String ENUM = Enum.class.getCanonicalName();

  private final AutoMapper autoMapper;
  private final ImmutableList<Matcher> matchers;
  private final SourceMethod sourceMethod;

  @Inject
  AutoConverterFinder(
      ParameterContext context,
      AutoMapper autoMapper,
      ImmutableList<Matcher> matchers,
      SourceMethod sourceMethod) {
    super(context);
    this.autoMapper = autoMapper;
    this.matchers = matchers;
    this.sourceMethod = sourceMethod;
  }

  public <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(P parameter) {
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch(parameter);
      if (match.isPresent()) {
        Match m = match.get();
        return Either.fromFailure(validateMatch(parameter, m), null)
            .flatMap(nothing -> findMapper(m, parameter));
      }
    }
    return left(noMatchError(sourceMethod.returnType()));
  }

  private <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findMapper(Match match, P parameter) {
    TypeMirror baseReturnType = match.baseReturnType();
    return autoMapper.findAutoMapper(baseReturnType)
        .maybeRecover(() -> isEnumType(baseReturnType) ?
            Optional.of(autoMapperEnum(baseReturnType)) :
            Optional.empty())
        .mapLeft(s -> noMatchError(baseReturnType))
        .map(mapExpr -> match.toCoercion(mapExpr, parameter));
  }

  private CodeBlock autoMapperEnum(TypeMirror baseReturnType) {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
    ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    return CodeBlock.builder()
        .add(".map(")
        .add("$N -> {\n", s).indent()
        .add("try {\n").indent()
        .add("return $T.valueOf($N);\n", baseReturnType, s)
        .unindent()
        .add("} catch ($T $N) {\n", IllegalArgumentException.class, e).indent()
        .add("$T $N = $T.stream($T.values())\n", STRING, values, Arrays.class, baseReturnType).indent()
        .add(".map($T::name)\n", baseReturnType)
        .add(".collect($T.joining($S, $S, $S));\n", Collectors.class, ", ", "[", "]")
        .unindent()
        .add("$T $N = $N.getMessage() + $S + $N;\n", STRING, message, e, " ", values)
        .add("throw new $T($N);\n", IllegalArgumentException.class, message)
        .unindent().add("}\n")
        .unindent().add("})\n").build();
  }

  private boolean isEnumType(TypeMirror type) {
    return types().directSupertypes(type).stream()
        .anyMatch(t -> tool().isSameErasure(t, ENUM));
  }

  private static String noMatchError(TypeMirror type) {
    return "define a converter that implements Function<String, " + Util.typeToString(type) + ">";
  }
}
