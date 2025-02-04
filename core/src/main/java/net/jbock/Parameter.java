package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a positional parameter.
 * The annotated method must be {@code abstract}
 * and have an empty argument list.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Parameter {

  /**
   * The parameter's index among all positional parameters.
   * {@link Option Named options} have no index and do not count.
   * The first parameter's index is {@code 0}.
   *
   * @return zero-based index
   */
  int index();

  /**
   * Declare a custom converter for this positional parameter.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} of such a function.
   *
   * @return converter class or {@code Void.class}
   */
  Class<?> converter() default Void.class;

  /**
   * The key that is used to find the parameter
   * description in the internationalization message map.
   * If no {@code descriptionKey} is defined,
   * or the runtime message map does not contain the description key,
   * then the {@code description} attribute will be used.
   * If that is also empty, the method's javadoc will be used as a fallback.
   *
   * @return description key or empty string
   */
  String descriptionKey() default "";

  /**
   * Parameter description, used when generating the usage documentation.
   * Can be overridden via {@code descriptionKey}.
   *
   * @return description text
   */
  String[] description() default {};

  /**
   * A label for this parameter, to be used in the usage documentation.
   * If empty, a label will be chosen based on the method name.
   *
   * @return a label
   */
  String paramLabel() default "";
}
