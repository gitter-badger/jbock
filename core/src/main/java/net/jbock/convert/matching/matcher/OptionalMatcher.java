package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.EnumName;
import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.convert.matching.MatchFactory;
import net.jbock.parameter.AbstractParameter;
import net.jbock.validate.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;

@ParameterScope
public class OptionalMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;
  private final Types types;
  private final Elements elements;
  private final MatchFactory matchFactory;

  @Inject
  OptionalMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      TypeTool tool,
      Types types,
      Elements elements,
      MatchFactory matchFactory) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.types = types;
    this.elements = elements;
    this.matchFactory = matchFactory;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    Optional<Match> optionalPrimitive = getOptionalPrimitive(returnType);
    if (optionalPrimitive.isPresent()) {
      return optionalPrimitive;
    }
    return tool.getSingleTypeArgument(returnType, Optional.class)
        .map(typeArg -> matchFactory.create(typeArg,
            constructorParam(returnType), Skew.OPTIONAL));
  }

  private Optional<Match> getOptionalPrimitive(TypeMirror type) {
    for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
      if (tool.isSameType(type, optionalPrimitive.type())) {
        ParameterSpec constructorParam = constructorParam(asOptional(optionalPrimitive));
        return Optional.of(matchFactory.create(
            elements.getTypeElement(optionalPrimitive.wrappedObjectType()).asType(),
            constructorParam,
            Skew.OPTIONAL,
            optionalPrimitive.extractExpr()));
      }
    }
    return Optional.empty();
  }

  private DeclaredType asOptional(OptionalPrimitive optionalPrimitive) {
    TypeElement optional = elements.getTypeElement(Optional.class.getCanonicalName());
    TypeElement element = elements.getTypeElement(optionalPrimitive.wrappedObjectType());
    return types.getDeclaredType(optional, element.asType());
  }
}
