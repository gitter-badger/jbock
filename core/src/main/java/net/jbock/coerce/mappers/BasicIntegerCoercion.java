package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicIntegerCoercion extends BasicNumberCoercion {

  BasicIntegerCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicIntegerCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::valueOf", Integer.class).build());
  }

}
