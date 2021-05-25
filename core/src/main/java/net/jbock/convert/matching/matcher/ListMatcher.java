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
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

@ParameterScope
public class ListMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;
  private final MatchFactory matchFactory;

  @Inject
  ListMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      TypeTool tool,
      MatchFactory matchFactory) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.matchFactory = matchFactory;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    ParameterSpec constructorParam = constructorParam(returnType);
    return tool.getSingleTypeArgument(returnType, List.class)
        .map(typeArg -> matchFactory.create(typeArg, constructorParam, Skew.REPEATABLE));
  }
}
