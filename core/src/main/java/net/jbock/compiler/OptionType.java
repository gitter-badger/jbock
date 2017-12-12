package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Parser.OptionType enum.
 *
 * @see Parser
 */
final class OptionType {

  final ClassName type;

  private final Context context;

  final FieldSpec isPositionalField;

  private OptionType(Context context) {
    this.context = context;
    this.isPositionalField = FieldSpec.builder(BOOLEAN, "positional", PRIVATE, FINAL)
        .build();
    this.type = context.generatedClass.nestedClass("OptionType");
  }

  static OptionType create(Context context) {
    return new OptionType(context);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (Type optionType : context.paramTypes) {
      addType(builder, optionType);
    }
    for (PositionalType optionType : context.positionalParamTypes) {
      addType(builder, optionType);
    }
    return builder.addModifiers(PUBLIC)
        .addField(isPositionalField)
        .addMethod(privateConstructor())
        .build();
  }

  private void addType(TypeSpec.Builder builder, Type optionType) {
    builder.addEnumConstant(optionType.name(),
        anonymousClassBuilder("$L", false).build());
  }

  private void addType(TypeSpec.Builder builder, PositionalType optionType) {
    builder.addEnumConstant(optionType.name(),
        anonymousClassBuilder("$L", true).build());
  }

  private MethodSpec privateConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    ParameterSpec positional = ParameterSpec.builder(BOOLEAN, isPositionalField.name).build();
    builder.addStatement("this.$N = $N", isPositionalField, positional);
    return builder.addParameter(positional).build();
  }
}
