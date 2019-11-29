package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

/**
 * Generates the RepeatablePositionalOptionParser class.
 */
final class RepeatablePositionalOptionParser {

  static TypeSpec define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .initializer("new $T<>()", ArrayList.class)
        .build();
    return TypeSpec.classBuilder(context.repeatablePositionalOptionParserType())
        .superclass(context.positionalOptionParserType())
        .addMethod(readMethod(values))
        .addMethod(MethodSpec.methodBuilder("values")
            .returns(STREAM_OF_STRING)
            .addStatement("return $N.stream()", values)
            .addAnnotation(Override.class)
            .build())
        .addField(values)
        .addModifiers(PRIVATE, STATIC).build();
  }

  private static MethodSpec readMethod(FieldSpec values) {
    ParameterSpec valueParam = ParameterSpec.builder(STRING, "value").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(valueParam)
        .returns(TypeName.INT)
        .addStatement("$N.add($N)", values, valueParam)
        .addStatement("return $L", 0)
        .addAnnotation(Override.class)
        .build();
  }
}
