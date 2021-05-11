package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.matching.matcher.ExactMatcher;
import net.jbock.convert.matching.matcher.ListMatcher;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.matching.matcher.OptionalMatcher;
import net.jbock.qualifier.ConverterClass;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.OptionType;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@Module
class ParameterModule {

  private final AnnotationUtil annotationUtil = new AnnotationUtil();

  private final OptionType optionType;
  private final TypeTool tool;
  private final ParserFlavour flavour;
  private final TypeElement sourceElement;
  private final DescriptionBuilder descriptionBuilder;

  ParameterModule(
      OptionType optionType,
      TypeTool tool,
      ParserFlavour flavour,
      TypeElement sourceElement,
      DescriptionBuilder descriptionBuilder) {
    this.optionType = optionType;
    this.tool = tool;
    this.flavour = flavour;
    this.sourceElement = sourceElement;
    this.descriptionBuilder = descriptionBuilder;
  }

  @Reusable
  @Provides
  EnumName enumName(
      SourceMethod sourceMethod,
      ImmutableList<ConvertedParameter<NamedOption>> alreadyCreated) {
    String methodName = sourceMethod.method().getSimpleName().toString();
    EnumName result = EnumName.create(methodName);
    for (ConvertedParameter<NamedOption> param : alreadyCreated) {
      if (param.enumName().enumConstant().equals(result.enumConstant())) {
        return result.append(Integer.toString(alreadyCreated.size()));
      }
    }
    return result;
  }

  @Reusable
  @Provides
  ImmutableList<Matcher> matchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }

  @Provides
  OptionType optionType() {
    return optionType;
  }

  @Provides
  TypeTool tool() {
    return tool;
  }

  @Provides
  Types types() {
    return tool.types();
  }

  @Provides
  Elements elements() {
    return tool.elements();
  }

  @Provides
  ParserFlavour flavour() {
    return flavour;
  }

  @Reusable
  @Provides
  SourceElement sourceElement() {
    return new SourceElement(sourceElement);
  }

  @Reusable
  @Provides
  DescriptionKey descriptionKey(SourceMethod sourceMethod, ParameterStyle parameterStyle) {
    return new DescriptionKey(parameterStyle.getParameterDescriptionKey(sourceMethod.method()));
  }

  @Reusable
  @Provides
  ConverterClass converter(SourceMethod sourceMethod) {
    return new ConverterClass(annotationUtil.getConverter(sourceMethod.method()));
  }

  @Reusable
  @Provides
  ParamLabel paramLabel(SourceMethod sourceMethod, ParameterStyle parameterStyle) {
    return new ParamLabel(parameterStyle.getParamLabel(sourceMethod.method()));
  }

  @Reusable
  @Provides
  Description description(SourceMethod sourceMethod) {
    return descriptionBuilder.getDescription(sourceMethod.method());
  }

  @Reusable
  @Provides
  ParameterStyle parameterStyle(SourceMethod sourceMethod) {
    return ParameterStyle.getStyle(sourceMethod.method());
  }
}
