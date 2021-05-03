package net.jbock.compiler;

import net.jbock.Command;
import net.jbock.SuperCommand;

import javax.lang.model.element.TypeElement;

public enum ParserFlavour {

  COMMAND {
    @Override
    public boolean helpEnabled(TypeElement sourceElement) {
      return sourceElement.getAnnotation(Command.class).helpEnabled();
    }

    @Override
    public String programName(TypeElement sourceElement) {
      if (!sourceElement.getAnnotation(Command.class).value().isEmpty()) {
        return sourceElement.getAnnotation(Command.class).value();
      }
      return EnumName.create(sourceElement.getSimpleName().toString()).snake('-');
    }

    @Override
    public boolean isSuperCommand() {
      return false;
    }
  },

  SUPER_COMMAND {
    @Override
    public boolean helpEnabled(TypeElement sourceElement) {
      return sourceElement.getAnnotation(SuperCommand.class).helpEnabled();
    }

    @Override
    public String programName(TypeElement sourceElement) {
      if (!sourceElement.getAnnotation(SuperCommand.class).value().isEmpty()) {
        return sourceElement.getAnnotation(SuperCommand.class).value();
      }
      return EnumName.create(sourceElement.getSimpleName().toString()).snake('-');
    }

    @Override
    public boolean isSuperCommand() {
      return true;
    }
  };

  public abstract boolean helpEnabled(TypeElement sourceElement);

  public abstract String programName(TypeElement sourceElement);

  public abstract boolean isSuperCommand();
}
