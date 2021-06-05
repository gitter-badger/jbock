package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.Util;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;
import net.jbock.util.ItemType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class BuildMethod extends Cached<MethodSpec> {

  private final GeneratedTypes generatedTypes;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;
  private final AnsiStyle styler;
  private final Util util;
  private final ParameterSpec left = ParameterSpec.builder(STRING, "left").build();

  @Inject
  BuildMethod(
      GeneratedTypes generatedTypes,
      SourceElement sourceElement,
      NamedOptions namedOptions,
      PositionalParameters positionalParameters,
      CommonFields commonFields,
      AnsiStyle styler,
      Util util) {
    this.generatedTypes = generatedTypes;
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
    this.styler = styler;
    this.util = util;
  }

  @Override
  MethodSpec define() {
    CodeBlock constructorArguments = getConstructorArguments();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");
    for (Mapped<NamedOption> c : namedOptions.options()) {
      ParameterSpec p = c.asParam();
      spec.addStatement("$T $N = $L", p.type, p, convertExpressionOption(c));
    }
    for (Mapped<PositionalParameter> c : positionalParameters.regular()) {
      ParameterSpec p = c.asParam();
      spec.addStatement("$T $N = $L", p.type, p, convertExpressionRegularParameter(c));
    }
    positionalParameters.repeatable().ifPresent(c -> {
      ParameterSpec p = c.asParam();
      spec.addStatement("$T $N = $L", p.type, p, convertExpressionRepeatableParameter(c));
    });
    generatedTypes.superResultType().ifPresentOrElse(parseResultWithRestType -> {
          ParameterSpec result = ParameterSpec.builder(sourceElement.typeName(), "result").build();
          ParameterSpec restArgs = ParameterSpec.builder(sourceElement.typeName(), "restArgs").build();
          spec.addStatement("$T $N = new $T($L)", result.type, result, generatedTypes.implType(),
              constructorArguments);
          spec.addStatement("$T $N = this.$N.toArray(new $T[0])", STRING_ARRAY, restArgs,
              commonFields.rest(), STRING);
          spec.addStatement("return new $T($N, $N)", parseResultWithRestType,
              result, restArgs);
        },
        () -> spec.addStatement("return new $T($L)", generatedTypes.implType(), constructorArguments));
    return spec.returns(generatedTypes.parseSuccessType())
        .build();
  }

  private CodeBlock getConstructorArguments() {
    List<CodeBlock> code = new ArrayList<>();
    for (Mapped<NamedOption> c : namedOptions.options()) {
      code.add(CodeBlock.of("$N", c.asParam()));
    }
    for (Mapped<PositionalParameter> c : positionalParameters.regular()) {
      code.add(CodeBlock.of("$N", c.asParam()));
    }
    positionalParameters.repeatable()
        .map(c -> CodeBlock.of("$N", c.asParam()))
        .ifPresent(code::add);
    return util.joinByComma(code);
  }

  private CodeBlock convertExpressionOption(Mapped<NamedOption> c) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(CodeBlock.of("this.$N.get($T.$N).stream()", commonFields.optionParsers(),
        sourceElement.itemType(), c.enumConstant()));
    if (!c.isFlag()) {
      code.add(c.mapExpr());
    }
    code.addAll(tailExpressionOption(c));
    c.extractExpr().ifPresent(code::add);
    return util.joinByNewline(code);
  }

  private CodeBlock convertExpressionRegularParameter(Mapped<PositionalParameter> c) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(CodeBlock.of("$T.ofNullable(this.$N[$L])", Optional.class, commonFields.params(),
        c.item().position()));
    code.add(c.mapExpr());
    code.addAll(tailExpressionParameter(c));
    c.extractExpr().ifPresent(code::add);
    return util.joinByNewline(code);
  }

  private CodeBlock convertExpressionRepeatableParameter(Mapped<PositionalParameter> c) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(CodeBlock.of("this.$N.stream()", commonFields.rest()));
    code.add(c.mapExpr());
    code.add(CodeBlock.of(".map(e -> e$L)", checkConverterError(c.item())));
    code.add(CodeBlock.of(".collect($T.toList())", Collectors.class));
    return util.joinByNewline(code);
  }

  private List<CodeBlock> tailExpressionOption(Mapped<NamedOption> c) {
    String optionNames = String.join(", ", c.item().names());
    String paramLabel = c.paramLabel();
    switch (c.skew()) {
      case REQUIRED:
        String message = "Missing required option: " + paramLabel + " (" + optionNames + ")";
        return List.of(
            CodeBlock.of(".findAny()"),
            CodeBlock.of(".orElseThrow(() -> new $T($S))", generatedTypes.syntExType(), message),
            checkConverterError(c.item()));
      case OPTIONAL:
        return List.of(
            CodeBlock.of(".map(e -> e$L)", checkConverterError(c.item())),
            CodeBlock.of(".findAny()"));
      case REPEATABLE:
        return List.of(
            CodeBlock.of(".map(e -> e$L)", checkConverterError(c.item())),
            CodeBlock.of(".collect($T.toList())", Collectors.class));
      case MODAL_FLAG:
        return List.of(CodeBlock.of(".findAny().isPresent()"));
      default:
        throw new IllegalArgumentException("unexpected skew: " + c.skew());
    }
  }

  private List<CodeBlock> tailExpressionParameter(Mapped<PositionalParameter> c) {
    switch (c.skew()) {
      case REQUIRED:
        String paramLabel = styler.bold(c.paramLabel()).orElse(c.paramLabel());
        return List.of(CodeBlock.of(".orElseThrow(() -> new $T($S))",
            generatedTypes.syntExType(), "Missing required parameter: " + paramLabel),
            checkConverterError(c.item()));
      case OPTIONAL:
        return List.of(CodeBlock.of(".map(e -> e$L)", checkConverterError(c.item())));
      default:
        throw new IllegalArgumentException("unexpected skew: " + c.skew());
    }
  }

  private CodeBlock checkConverterError(NamedOption option) {
    String itemName = option.paramLabel() + " (" + String.join(", ", option.names()) + ")";
    return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5S))",
        left, generatedTypes.convExType(),
        ItemType.class,
        ItemType.OPTION,
        itemName);
  }

  private CodeBlock checkConverterError(PositionalParameter parameter) {
    String itemName = parameter.paramLabel();
    return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5S))",
        left, generatedTypes.convExType(),
        ItemType.class,
        ItemType.PARAMETER,
        itemName);
  }
}
