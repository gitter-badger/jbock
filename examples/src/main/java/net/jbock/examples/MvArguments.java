package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

@Command(ansi = false)
abstract class MvArguments implements MvArguments_Parent {

  @Parameter(index = 1)
  abstract String dest();

  @Override
  public boolean isSafe() {
    return true;
  }
}
