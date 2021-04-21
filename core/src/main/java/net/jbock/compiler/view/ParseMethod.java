package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;

import javax.inject.Inject;
import java.util.ArrayList;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

class ParseMethod {

  private final Context context;
  private final GeneratedTypes generatedTypes;

  private final ParameterSpec state;
  private final ParameterSpec it;
  private final ParameterSpec option;
  private final ParameterSpec token;
  private final ParameterSpec position;
  private final ParameterSpec endOfOptionParsing;
  private final ParserState parserState;

  @Inject
  ParseMethod(Context context, GeneratedTypes generatedTypes, ParserState parserState) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.state = builder(generatedTypes.parserStateType(), "state").build();
    this.parserState = parserState;
    this.it = builder(STRING_ITERATOR, "it").build();
    this.option = builder(generatedTypes.optionType(), "option").build();
    this.token = builder(STRING, "token").build();
    this.position = builder(INT, "position").build();
    this.endOfOptionParsing = builder(BOOLEAN, "endOfOptionParsing").build();
  }

  MethodSpec parseMethod() {

    return MethodSpec.methodBuilder("parse")
        .addParameter(it)
        .addCode(generatedTypes.parseResultWithRestType()
            .map(this::superCommandCode)
            .orElseGet(this::regularCode))
        .addModifiers(PRIVATE)
        .returns(generatedTypes.parseSuccessType())
        .build();
  }

  private CodeBlock superCommandCode(ClassName parseResultWithRestType) {
    CodeBlock.Builder code = initVariables();
    ParameterSpec rest = builder(LIST_OF_STRING, "rest").build();
    code.addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class);

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if ($N)", endOfOptionParsing)
        .addStatement("$N.add($N)", rest, token)
        .addStatement("continue")
        .endControlFlow();

    if (!context.options().isEmpty()) {
      code.add(optionBlock());
    }
    code.add(handleDashTokenBlock());

    code.addStatement(readParamCode());

    code.add("if ($N == $L)\n", position, context.params().size()).indent()
        .addStatement("$N = $L", endOfOptionParsing, true)
        .unindent();

    // end parsing loop
    code.endControlFlow();

    code.addStatement("return new $T($N.build(), $N.toArray(new $T[0]))",
        parseResultWithRestType, state, rest, String.class);
    return code.build();
  }

  private CodeBlock regularCode() {
    CodeBlock.Builder code = initVariables();

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if (!$N && $S.equals($N))", endOfOptionParsing, "--", token)
        .addStatement("$N = $L", endOfOptionParsing, true)
        .addStatement("continue")
        .endControlFlow();

    code.beginControlFlow("if (!$N)", endOfOptionParsing);
    if (!context.options().isEmpty()) {
      code.add(optionBlock());
    }
    code.add(handleDashTokenBlock());
    code.endControlFlow();

    code.add("if ($N == $L)\n", position, context.params().size()).indent()
        .addStatement(throwInvalidOptionStatement("Excess param"))
        .unindent();

    if (!context.params().isEmpty()) {
      code.addStatement(readParamCode());
    }

    // end parsing loop
    code.endControlFlow();

    return code.addStatement("return $N.build()", state).build();
  }

  private CodeBlock optionBlock() {
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.$N($N)", generatedTypes.optionType(), option, state, parserState.tryReadOption(), token);
    code.beginControlFlow("if ($N != null)", option)
        .addStatement("$N.$N.get($N).read($N, $N, $N)", state, parserState.parsersField(), option, option, token, it)
        .addStatement("continue")
        .endControlFlow();
    return code.build();
  }

  private CodeBlock handleDashTokenBlock() {
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N.startsWith($S))\n", token, "-").indent()
        .addStatement(throwInvalidOptionStatement("Invalid option"))
        .unindent();
    return code.build();
  }

  private CodeBlock.Builder initVariables() {
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $L", position.type, position, 0);
    code.addStatement("$T $N = new $T()", state.type, state, state.type);
    code.addStatement("$T $N = $L", endOfOptionParsing.type, endOfOptionParsing, false);
    return code;
  }

  private CodeBlock readParamCode() {
    return CodeBlock.of("$N += $N.$N.get($N).read($N)",
        position, state, parserState.positionalParsersField(), position, token);
  }

  private CodeBlock throwInvalidOptionStatement(String message) {
    return CodeBlock.of("throw new $T($S + $N)", RuntimeException.class, message + ": ", token);
  }
}
