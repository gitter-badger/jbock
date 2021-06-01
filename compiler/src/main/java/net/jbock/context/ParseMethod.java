package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.Arrays;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.STRING_ARRAY;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ParseMethod extends Cached<MethodSpec> {

  private final GeneratedTypes generatedTypes;
  private final AllParameters allParameters;
  private final SourceElement sourceElement;
  private final BuildMethod buildMethod;
  private final AtFileReader atFileReader;
  private final ReadAtFileMethod readAtFileMethod;

  @Inject
  ParseMethod(
      GeneratedTypes generatedTypes,
      AllParameters allParameters,
      SourceElement sourceElement,
      BuildMethod buildMethod,
      AtFileReader atFileReader,
      ReadAtFileMethod readAtFileMethod) {
    this.generatedTypes = generatedTypes;
    this.allParameters = allParameters;
    this.sourceElement = sourceElement;
    this.buildMethod = buildMethod;
    this.atFileReader = atFileReader;
    this.readAtFileMethod = readAtFileMethod;
  }

  @Override
  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec e = builder(Exception.class, "e").build();
    CodeBlock.Builder code = CodeBlock.builder();

    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> {
      if (allParameters.anyRequired()) {
        code.add("if ($N.length == 0)\n",
            args).indent()
            .addStatement("return new $T()", helpRequestedType)
            .unindent();
      }
      code.add("if ($1N.length == 1 && $2S.equals($1N[0]))\n",
          args, "--help").indent()
          .addStatement("return new $T()", helpRequestedType)
          .unindent();
    });
    code.beginControlFlow("try");
    ParameterSpec state = builder(generatedTypes.statefulParserType(), "statefulParser").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec result = builder(generatedTypes.parseSuccessType(), "result").build();
    code.addStatement("$T $N = new $T()", state.type, state, state.type);
    if (sourceElement.expandAtSign()) {
      code.addStatement("$T $N", it.type, it);
      code.beginControlFlow("if ($1N.length == 1 && $1N[0].length() >= 2 && $1N[0].startsWith($2S))", args, "@");
      code.addStatement("$N = new $T().$N($N[0].substring(1)).iterator()", it, sourceElement.atFileReaderType(),
          readAtFileMethod.get(), args);
      code.endControlFlow();
      code.beginControlFlow("else");
      code.addStatement("$N = $T.asList($N).iterator()", it, Arrays.class, args);
      code.endControlFlow();
    } else {
      code.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);
    }
    code.addStatement("$T $N = $N.parse($N).$N()", result.type, result, state, it, buildMethod.get());
    code.addStatement("return new $T($N)", generatedTypes.parsingSuccessWrapperType(), result);
    code.endControlFlow();

    code.beginControlFlow("catch ($T $N)", Exception.class, e)
        .addStatement("return new $T($N)",
            generatedTypes.parsingFailedType(), e)
        .endControlFlow();

    return MethodSpec.methodBuilder("parse").addParameter(args)
        .returns(generatedTypes.parseResultType())
        .addCode(code.build())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }
}
