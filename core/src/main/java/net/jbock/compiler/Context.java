package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the class that will be generated
  private final ClassName generatedClass;

  // the abstract methods in the annotated class
  private final List<Coercion<? extends AbstractParameter>> parameters;

  private final List<Coercion<PositionalParameter>> params;

  private final Optional<Coercion<PositionalParameter>> repeatableParam;

  private final List<Coercion<? extends AbstractParameter>> regularParameters;

  private final List<Coercion<NamedOption>> options;

  private final ParserFlavour flavour;

  private final GeneratedTypes generatedTypes;

  private final boolean unixClusteringSupported;

  @Inject
  Context(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Coercion<NamedOption>> namedOptions,
      List<Coercion<PositionalParameter>> params,
      ParserFlavour flavour,
      GeneratedTypes generatedTypes) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.params = params;
    this.options = namedOptions;
    this.unixClusteringSupported = isUnixClusteringSupported(namedOptions);
    this.flavour = flavour;
    this.parameters = ImmutableList.<Coercion<? extends AbstractParameter>>builder().addAll(options).addAll(params).build();
    this.repeatableParam = params.stream()
        .filter(Coercion::isRepeatable)
        .findFirst();
    this.regularParameters = parameters.stream().filter(c -> !c.isRepeatable()).collect(Collectors.toList());
    this.generatedTypes = generatedTypes;
  }

  public Modifier[] getAccessModifiers() {
    return sourceElement.getModifiers().stream().filter(ALLOWED_MODIFIERS::contains).toArray(Modifier[]::new);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public List<Coercion<? extends AbstractParameter>> parameters() {
    return parameters;
  }

  public List<Coercion<PositionalParameter>> params() {
    return params;
  }

  public List<Coercion<NamedOption>> options() {
    return options;
  }

  public boolean isHelpParameterEnabled() {
    return flavour.helpEnabled(sourceElement);
  }

  public String programName() {
    return flavour.programName(sourceElement);
  }

  public FieldSpec exitHookField() {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(ClassName.get(BiConsumer.class),
        generatedTypes.parseResultType(), ClassName.get(Integer.class));
    ParameterSpec result = ParameterSpec.builder(generatedTypes.parseResultType(), "result").build();
    ParameterSpec rc = ParameterSpec.builder(Integer.class, "rc").build();
    return FieldSpec.builder(consumer, "exitHook")
        .addModifiers(PRIVATE)
        .initializer("($N, $N) -> $T.exit($N)", result, rc, System.class, rc)
        .build();
  }

  public boolean isSuperCommand() {
    return flavour.isSuperCommand();
  }

  public boolean anyRepeatableParam() {
    return repeatableParam.isPresent();
  }

  public boolean isUnixClusteringSupported() {
    return unixClusteringSupported;
  }

  private static boolean isUnixClusteringSupported(List<Coercion<NamedOption>> options) {
    List<Coercion<NamedOption>> unixOptions = options.stream()
        .filter(option -> option.parameter().hasUnixName())
        .collect(Collectors.toList());
    return unixOptions.size() >= 2 && unixOptions.stream().anyMatch(Coercion::isFlag);
  }

  public String getSuccessResultMethodName() {
    return isSuperCommand() ? "getResultWithRest" : "getResult";
  }

  public Optional<Coercion<PositionalParameter>> repeatableParam() {
    return repeatableParam;
  }

  /**
   * Everything but the repeatable param.
   */
  public List<Coercion<? extends AbstractParameter>> regularParameters() {
    return regularParameters;
  }
}
