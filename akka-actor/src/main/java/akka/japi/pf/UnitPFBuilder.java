/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.japi.pf;

import scala.runtime.BoxedUnit;

/**
 * A builder for {@link scala.PartialFunction}.
 * This is a specialized version of {@link PFBuilder} to map java
 * void methods to {@link scala.runtime.BoxedUnit}.
 *
 * @param <I> the input type, that this PartialFunction to be applied to
 *
 * This is an EXPERIMENTAL feature and is subject to change until it has received more real world testing.
 */
public final class UnitPFBuilder<I> extends AbstractPFBuilder<I, BoxedUnit> {

  /**
   * Create a UnitPFBuilder.
   */
  public UnitPFBuilder() {
  }

  /**
   * Add a new case statement to this builder.
   *
   * @param type  a type to match the argument against
   * @param apply an action to apply to the argument if the type matches
   * @return a builder with the case statement added
   */
  @SuppressWarnings("unchecked")
  public <P> UnitPFBuilder<I> match(final Class<? extends P> type,
                                    final FI.UnitApply<? extends P> apply) {

    FI.Predicate predicate = new FI.Predicate() {
      @Override
      public boolean defined(Object o) {
        return type.isInstance(o);
      }
    };

    addStatement(new UnitCaseStatement<I, P>(predicate, (FI.UnitApply<P>) apply));

    return this;
  }

  /**
   * Add a new case statement to this builder.
   *
   * @param type      a type to match the argument against
   * @param predicate a predicate that will be evaluated on the argument if the type matches
   * @param apply     an action to apply to the argument if the type matches and the predicate returns true
   * @return a builder with the case statement added
   */
  @SuppressWarnings("unchecked")
  public <P> UnitPFBuilder<I> match(final Class<? extends P> type,
                                    final FI.TypedPredicate<? extends P> predicate,
                                    final FI.UnitApply<? extends P> apply) {
    FI.Predicate fiPredicate = new FI.Predicate() {
      @Override
      public boolean defined(Object o) {
        if (!type.isInstance(o))
          return false;
        else {
          @SuppressWarnings("unchecked")
          P p = (P) o;
          return ((FI.TypedPredicate<P>) predicate).defined(p);
        }
      }
    };

    addStatement(new UnitCaseStatement<I, P>(fiPredicate, (FI.UnitApply<P>) apply));

    return this;
  }

  /**
   * Add a new case statement to this builder.
   *
   * @param object the object to compare equals with
   * @param apply  an action to apply to the argument if the object compares equal
   * @return a builder with the case statement added
   */
  public <P> UnitPFBuilder<I> matchEquals(final P object,
                                          final FI.UnitApply<P> apply) {
    addStatement(new UnitCaseStatement<I, P>(
      new FI.Predicate() {
        @Override
        public boolean defined(Object o) {
          return object.equals(o);
        }
      }, apply));
    return this;
  }

  /**
   * Add a new case statement to this builder.
   *
   * @param object    the object to compare equals with
   * @param predicate a predicate that will be evaluated on the argument if the object compares equal
   * @param apply     an action to apply to the argument if the object compares equal
   * @return a builder with the case statement added
   */
  public <P> UnitPFBuilder<I> matchEquals(final P object,
                                          final FI.TypedPredicate<P> predicate,
                                          final FI.UnitApply<P> apply) {
    addStatement(new UnitCaseStatement<I, P>(
      new FI.Predicate() {
        @Override
        public boolean defined(Object o) {
          if (!object.equals(o))
            return false;
          else {
            @SuppressWarnings("unchecked")
            P p = (P) o;
            return predicate.defined(p);
          }
        }
      }, apply));
    return this;
  }

  /**
   * Add a new case statement to this builder, that matches any argument.
   *
   * @param apply an action to apply to the argument
   * @return a builder with the case statement added
   */
  public UnitPFBuilder<I> matchAny(final FI.UnitApply<Object> apply) {
    addStatement(new UnitCaseStatement<I, Object>(
      new FI.Predicate() {
        @Override
        public boolean defined(Object o) {
          return true;
        }
      }, apply));
    return this;
  }
}
