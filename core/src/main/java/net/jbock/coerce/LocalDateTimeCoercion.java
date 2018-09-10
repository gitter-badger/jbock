package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

class LocalDateTimeCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", LocalDateTime.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(LocalDateTime.class);
  }
}
