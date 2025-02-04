package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.common.EnumName;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.NamedOption;
import net.jbock.util.StringConverter;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public final class Mapped<P extends AbstractItem> {

  private final CodeBlock mapExpr;
  private final Optional<CodeBlock> extractExpr;
  private final Multiplicity skew;
  private final P item;
  private final ParameterSpec asParameterSpec;
  private final FieldSpec asFieldSpec;
  private final boolean modeFlag;

  private Mapped(
      CodeBlock mapExpr,
      Optional<CodeBlock> extractExpr,
      Multiplicity skew,
      ParameterSpec asParameterSpec,
      FieldSpec asFieldSpec,
      P item,
      boolean modeFlag) {
    this.asParameterSpec = asParameterSpec;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.asFieldSpec = asFieldSpec;
    this.item = item;
    this.modeFlag = modeFlag;
  }

  public static <P extends AbstractItem> Mapped<P> create(
      CodeBlock mapExpr,
      Optional<CodeBlock> extractExpr,
      Multiplicity skew,
      P parameter) {
    TypeName fieldType = parameter.returnType();
    String fieldName = '_' + parameter.enumName().enumConstant().toLowerCase(Locale.US);
    FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
    ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
    return new Mapped<>(mapExpr, extractExpr, skew, asParameterSpec,
        asFieldSpec, parameter, false);
  }

  public static Mapped<NamedOption> createFlag(NamedOption namedOption) {
    CodeBlock mapExpr = CodeBlock.of(".map($T.create($T.identity()))", StringConverter.class, Function.class);
    TypeName fieldType = TypeName.BOOLEAN;
    String fieldName = '_' + namedOption.enumName().enumConstant().toLowerCase(Locale.US);
    FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
    ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
    return new Mapped<>(mapExpr, Optional.empty(), Multiplicity.OPTIONAL, asParameterSpec,
        asFieldSpec, namedOption, true);
  }


  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public Optional<CodeBlock> extractExpr() {
    return extractExpr;
  }

  public Multiplicity multiplicity() {
    return skew;
  }

  public EnumName enumName() {
    return item.enumName();
  }

  public boolean isRequired() {
    return skew == Multiplicity.REQUIRED;
  }

  public boolean isRepeatable() {
    return skew == Multiplicity.REPEATABLE;
  }

  public boolean isOptional() {
    return skew == Multiplicity.OPTIONAL;
  }

  public boolean isFlag() {
    return modeFlag;
  }

  public P item() {
    return item;
  }

  public String paramLabel() {
    return item.paramLabel();
  }

  public String enumConstant() {
    return enumName().enumConstant();
  }

  public FieldSpec asField() {
    return asFieldSpec;
  }

  public ParameterSpec asParam() {
    return asParameterSpec;
  }
}
