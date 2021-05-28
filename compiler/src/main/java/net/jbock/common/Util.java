package net.jbock.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Util {

  private final Types types;
  private final TypeTool tool;

  public Util(Types types, TypeTool tool) {
    this.types = types;
    this.tool = tool;
  }

  public <E> List<E> concat(List<? extends E> list1, List<? extends E> list2) {
    List<E> result = new ArrayList<>(list1.size() + list2.size());
    result.addAll(list1);
    result.addAll(list2);
    return result;
  }

  public Optional<String> commonTypeChecks(TypeElement classToCheck) {
    if (classToCheck.getNestingKind().isNested() && !classToCheck.getModifiers().contains(Modifier.STATIC)) {
      return Optional.of("nested class must be static");
    }
    for (TypeElement element : getEnclosingElements(classToCheck)) {
      if (element.getModifiers().contains(Modifier.PRIVATE)) {
        return Optional.of("class cannot be private");
      }
    }
    if (!hasDefaultConstructor(classToCheck)) {
      return Optional.of("default constructor not found");
    }
    return Optional.empty();
  }

  private boolean hasDefaultConstructor(TypeElement classToCheck) {
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
    if (constructors.isEmpty()) {
      return true;
    }
    for (ExecutableElement constructor : constructors) {
      if (!constructor.getModifiers().contains(Modifier.PRIVATE) &&
          constructor.getParameters().isEmpty() &&
          !throwsAnyCheckedExceptions(constructor)) {
        return true;
      }
    }
    return false;
  }

  public boolean throwsAnyCheckedExceptions(ExecutableElement element) {
    for (TypeMirror thrownType : element.getThrownTypes()) {
      if (!extendsRuntimeException(thrownType)) {
        return true;
      }
    }
    return false;
  }

  private boolean extendsRuntimeException(TypeMirror mirror) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return false;
    }
    if (tool.isSameType(mirror, RuntimeException.class.getCanonicalName())) {
      return true;
    }
    TypeElement el = TypeTool.AS_TYPE_ELEMENT.visit(types.asElement(mirror));
    return extendsRuntimeException(el.getSuperclass());
  }

  public List<TypeElement> getEnclosingElements(TypeElement sourceElement) {
    List<TypeElement> result = new ArrayList<>();
    TypeElement current = sourceElement;
    result.add(current);
    while (current.getNestingKind() == NestingKind.MEMBER) {
      Element enclosingElement = current.getEnclosingElement();
      if (enclosingElement.getKind() != ElementKind.CLASS) {
        return result;
      }
      current = TypeTool.AS_TYPE_ELEMENT.visit(enclosingElement);
      result.add(current);
    }
    return result;
  }

  public String typeToString(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return type.toString();
    }
    DeclaredType declared = TypeTool.AS_DECLARED.visit(type);
    String base = TypeTool.AS_TYPE_ELEMENT.visit(declared.asElement()).getSimpleName().toString();
    if (declared.getTypeArguments().isEmpty()) {
      return base;
    }
    return base + declared.getTypeArguments().stream().map(this::typeToString)
        .collect(Collectors.joining(", ", "<", ">"));
  }

  public Optional<String> assertAtLeastOneAnnotation(
      Element element,
      List<Class<? extends Annotation>> annotations) {
    for (Class<? extends Annotation> annotation : annotations) {
      if (element.getAnnotation(annotation) != null) {
        return Optional.empty();
      }
    }
    return Optional.of("add one of these annotations: " + annotations.stream()
        .map(ann -> "@" + ann.getSimpleName())
        .collect(Collectors.joining(", ")));
  }

  public Optional<String> assertNoDuplicateAnnotations(
      Element element,
      Class<? extends Annotation> ann1,
      Class<? extends Annotation> ann2,
      Class<? extends Annotation> ann3) {
    return assertNoDuplicateAnnotations(element, List.of(ann1, ann2, ann3));
  }

  public Optional<String> assertNoDuplicateAnnotations(
      Element element,
      List<Class<? extends Annotation>> annotations) {
    Class<?> found = null;
    for (Class<? extends Annotation> annotation : annotations) {
      if (element.getAnnotation(annotation) != null) {
        if (found != null) {
          return Optional.of("annotate with either @" + found.getSimpleName() +
              " or @" + annotation.getSimpleName() + " but not both");
        }
        found = annotation;
      }
    }
    return Optional.empty();
  }
}
