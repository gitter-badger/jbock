package net.jbock.compiler.parameter;

import net.jbock.compiler.EnumName;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceMethod;

import java.util.Locale;

public class PositionalParameter extends AbstractParameter {

  // for @Parameter this is the index
  // for @Parameters, greatest index plus one
  private final int position;
  private final ParamLabel paramLabel;

  public PositionalParameter(
      SourceMethod sourceMethod,
      EnumName enumName,
      int position,
      ParamLabel paramLabel) {
    super(sourceMethod, enumName);
    this.position = position;
    this.paramLabel = paramLabel;
  }

  public String paramLabel() {
    return paramLabel.label().orElse(enumName().snake().toUpperCase(Locale.US));
  }

  public int position() {
    return position;
  }
}
