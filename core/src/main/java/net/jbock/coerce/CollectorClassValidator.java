package net.jbock.coerce;

import net.jbock.coerce.collector.CustomCollector;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.Util.checkNotAbstract;
import static net.jbock.coerce.reference.ExpectedType.COLLECTOR;

class CollectorClassValidator {

  private final BasicInfo basicInfo;
  private final TypeElement collectorClass;

  CollectorClassValidator(BasicInfo basicInfo, TypeElement collectorClass) {
    this.basicInfo = basicInfo;
    this.collectorClass = collectorClass;
  }

  // visible for testing
  CustomCollector getCollectorInfo() {
    commonChecks(collectorClass);
    checkNotAbstract(collectorClass);
    ReferencedType<Collector> collectorType = new ReferenceTool<>(COLLECTOR, basicInfo, collectorClass)
        .getReferencedType();
    TypeMirror t = collectorType.typeArguments().get(0);
    TypeMirror r = collectorType.typeArguments().get(2);
    Map<String, TypeMirror> r_result = tool().unify(basicInfo.originalReturnType(), r)
        .orElseThrow(() -> boom(String.format("The collector should return %s but returns %s", basicInfo.originalReturnType(), r)));
    Optional<? extends TypeMirror> inputType = tool().substitute(t, r_result);
    if (!inputType.isPresent()) {
      throw boom("could not resolve all type parameters");
    }
    List<TypeMirror> typeParameters = new Flattener(basicInfo, collectorClass)
        .getTypeParameters(r_result)
        .orElseThrow(this::boom);
    return new CustomCollector(tool(), inputType.get(), collectorClass,
        collectorType.isSupplier(), typeParameters);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(COLLECTOR.boom(message));
  }
}
