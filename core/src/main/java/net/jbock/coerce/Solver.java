package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Solver {

  private final BasicInfo basicInfo;
  private final TypeElement targetElement;

  public Solver(BasicInfo basicInfo, TypeElement targetElement) {
    this.targetElement = targetElement;
    this.basicInfo = basicInfo;
  }

  /**
   * @param results named type parameters
   * @return type parameters in the correct order for {@code targetElement}
   */
  public Either<List<TypeMirror>, String> solve(List<Map<String, TypeMirror>> results) {
    Either<Map<String, TypeMirror>, String> result = mergeResult(results);
    if (result instanceof Right) {
      return Either.right(((Right<Map<String, TypeMirror>, String>) result).value());
    }
    Map<String, TypeMirror> value = ((Left<Map<String, TypeMirror>, String>) result).value();
    List<? extends TypeParameterElement> typeParameters = targetElement.getTypeParameters();
    List<TypeMirror> outcome = new ArrayList<>();
    for (TypeParameterElement p : typeParameters) {
      Either<TypeMirror, String> solution = getSolution(value, p);
      if (solution instanceof Right) {
        return Either.right(((Right<TypeMirror, String>) solution).value());
      }
      outcome.add(((Left<TypeMirror, String>) solution).value());
    }
    return Either.left(outcome);
  }

  private Either<TypeMirror, String> getSolution(Map<String, TypeMirror> result, TypeParameterElement typeParameter) {
    TypeMirror m = result.get(typeParameter.toString());
    List<? extends TypeMirror> bounds = typeParameter.getBounds();
    if (m != null) {
      if (tool().isOutOfBounds(m, bounds)) {
        return Either.right("invalid bounds");
      }
    }
    return Either.left(m);
  }

  private Either<Map<String, TypeMirror>, String> mergeResult(List<Map<String, TypeMirror>> results) {
    Map<String, TypeMirror> out = new LinkedHashMap<>();
    for (Map<String, TypeMirror> result : results) {
      for (Map.Entry<String, TypeMirror> entry : result.entrySet()) {
        TypeMirror current = out.get(entry.getKey());
        if (current != null) {
          if (!tool().isSameType(current, entry.getValue())) {
            return Either.right("invalid bounds");
          }
        }
        out.put(entry.getKey(), entry.getValue());
      }
    }
    return Either.left(out);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
