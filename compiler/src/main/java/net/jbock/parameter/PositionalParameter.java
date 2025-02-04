package net.jbock.parameter;

import net.jbock.common.EnumName;

import java.util.Locale;

public final class PositionalParameter extends AbstractItem {

  // for @Parameter this is the index
  // for @Parameters, greatest index plus one
  private final int position;

  public PositionalParameter(
      SourceMethod sourceMethod,
      EnumName enumName,
      int position) {
    super(sourceMethod, enumName);
    this.position = position;
  }

  @Override
  public final String paramLabel() {
    return sourceMethod().paramLabel()
        .orElse(enumName().snake('_').toUpperCase(Locale.US));
  }

  public int position() {
    return position;
  }
}
