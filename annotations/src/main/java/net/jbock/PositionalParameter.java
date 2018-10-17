package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * <h3>Marker for positional parameter methods</h3>
 *
 * <ul>
 * <li>The annotated method must be abstract and have an empty argument list.</li>
 * <li>The annotated method may not carry the {@link Parameter} annotation.</li>
 * </ul>
 *
 * <p>For example, the following shell commands are passing positional parameters:</p>
 * <pre>{@code
 * ls ..
 * git log
 * echo 'First positional parameter' 'Second positional parameter'
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {

  /**
   * <p>The parameter position in the sequence of all positional parameters.
   * Lower order parameters come first.</p>
   *
   * <ul>
   * <li>Each parameter must have a unique position.</li>
   * <li>Gaps in the position numbers are allowed. Negative numbers are also allowed.</li>
   * <li>Required parameters must have the lowest positions.</li>
   * <li>There can only be one {@link #repeatable()} positional parameter, and it must have the highest position.</li>
   * </ul>
   *
   * @return parameter position
   */
  int position();

  /**
   * Defines the description argument name.
   * See {@link Parameter#descriptionArgumentName()}.
   *
   * @return an optional name that's used in the parameter description
   */
  String descriptionArgumentName() default "";

  /**
   * Optional custom mapper.
   * See {@link Parameter#mappedBy()}.
   *
   * @return an optional mapper class
   */
  Class<? extends Supplier> mappedBy() default Supplier.class;

  /**
   * Optional custom collector.
   * See {@link Parameter#collectedBy()}.
   *
   * @return an optional collector class
   */
  Class<? extends Supplier> collectedBy() default Supplier.class;

  /**
   * Declares this parameter repeatable.
   * See {@link Parameter#repeatable()}.
   *
   * @return true if this parameter is repeatable
   */
  boolean repeatable() default false;

  /**
   * Declares this parameter optional.
   * See {@link Parameter#optional()}.
   *
   * @return true if this parameter is optional
   */
  boolean optional() default false;

  /**
   * Defines a bundle key.
   * See {@link Parameter#bundleKey()}.
   *
   * @return an optional resource bundle key
   */
  String bundleKey() default "";
}
