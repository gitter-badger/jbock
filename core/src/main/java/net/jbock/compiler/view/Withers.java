package net.jbock.compiler.view;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.qualifier.AnyDescriptionKeys;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.SourceElement;
import net.jbock.scope.ContextScope;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;

@ContextScope
public class Withers {

  private final CommonFields commonFields;
  private final SourceElement sourceElement;
  private final AnyDescriptionKeys anyDescriptionKeys;

  @Inject
  Withers(
      CommonFields commonFields,
      SourceElement sourceElement,
      AnyDescriptionKeys anyDescriptionKeys) {
    this.commonFields = commonFields;
    this.sourceElement = sourceElement;
    this.anyDescriptionKeys = anyDescriptionKeys;
  }

  MethodSpec withTerminalWidthMethod() {
    ParameterSpec width = builder(commonFields.terminalWidth().type, "width").build();
    return methodBuilder("withTerminalWidth")
        .addParameter(width)
        .addStatement("this.$1N = $2N == 0 ? this.$1N : $2N", commonFields.terminalWidth(), width)
        .addStatement("return this")
        .returns(sourceElement.generatedClass())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  MethodSpec withExitHookMethod() {
    ParameterSpec param = builder(commonFields.exitHook().type,
        commonFields.exitHook().name).build();
    return methodBuilder("withExitHook")
        .addParameter(param)
        .addStatement("this.$N = $N", commonFields.exitHook(), param)
        .addStatement("return this")
        .returns(sourceElement.generatedClass())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  MethodSpec withMessagesMethod() {
    ParameterSpec resourceBundleParam = builder(commonFields.messages().type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    spec.addParameter(resourceBundleParam);
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addStatement("this.$N = $N", commonFields.messages(), resourceBundleParam);
    }
    spec.addStatement("return this");
    return spec.returns(sourceElement.generatedClass())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  MethodSpec withErrorStreamMethod() {
    ParameterSpec param = builder(commonFields.err().type, commonFields.err().name).build();
    return methodBuilder("withErrorStream")
        .addParameter(param)
        .addStatement("this.$N = $N", commonFields.err(), param)
        .addStatement("return this")
        .returns(sourceElement.generatedClass())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }
}
