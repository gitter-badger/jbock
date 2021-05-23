package net.jbock.qualifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ParserFlavour;
import net.jbock.compiler.ValidationFailure;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ACCESS_MODIFIERS;

public class SourceElement {

  private final TypeElement sourceElement;
  private final ParserFlavour parserFlavour;
  private final Set<Modifier> accessModifiers;
  private final String programName;
  private final ClassName generatedClass;
  private final ClassName optionType;

  private SourceElement(
      TypeElement sourceElement,
      ParserFlavour parserFlavour,
      Set<Modifier> accessModifiers,
      String programName,
      ClassName generatedClass,
      ClassName optionType) {
    this.sourceElement = sourceElement;
    this.parserFlavour = parserFlavour;
    this.accessModifiers = accessModifiers;
    this.programName = programName;
    this.generatedClass = generatedClass;
    this.optionType = optionType;
  }

  public static SourceElement create(TypeElement typeElement, ParserFlavour parserFlavour) {
    Set<Modifier> accessModifiers = typeElement.getModifiers().stream()
        .filter(ACCESS_MODIFIERS::contains)
        .collect(Collectors.toSet());
    String programName = parserFlavour.programName(typeElement)
        .orElseGet(() -> EnumName.create(typeElement.getSimpleName().toString()).snake('-'));
    String generatedClassName = String.join("_", ClassName.get(typeElement).simpleNames()) + "_Parser";
    ClassName generatedClass = ClassName.get(typeElement)
        .topLevelClassName()
        .peerClass(generatedClassName);
    ClassName optionType = generatedClass.nestedClass("Option");
    return new SourceElement(typeElement, parserFlavour, accessModifiers,
        programName, generatedClass, optionType);
  }

  public TypeElement element() {
    return sourceElement;
  }

  public TypeName typeName() {
    return TypeName.get(sourceElement.asType());
  }

  public ValidationFailure fail(String message) {
    return new ValidationFailure(message, sourceElement);
  }

  public boolean isSuperCommand() {
    return parserFlavour.isSuperCommand();
  }

  public boolean helpEnabled() {
    return parserFlavour.helpEnabled(sourceElement);
  }

  public boolean isAnsi() {
    return parserFlavour.isAnsi(sourceElement);
  }

  public String programName() {
    return programName;
  }

  public String resultMethodName() {
    return isSuperCommand() ? "getResultWithRest" : "getResult";
  }

  public Set<Modifier> accessModifiers() {
    return accessModifiers;
  }

  public Optional<String> descriptionKey() {
    return parserFlavour.descriptionKey(sourceElement);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public ClassName optionType() {
    return optionType;
  }

  public List<String> description(Elements elements) {
    return parserFlavour.description(sourceElement, elements);
  }
}
