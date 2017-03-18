package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/**
 * Encapsulation of modifiers allowed on a type/field/method.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class Modifiers {
  public abstract Visibility visibility();

  public abstract boolean isAbstract();

  public abstract boolean isStatic();

  public abstract boolean isFinal();

  private Modifiers validate() {
    Preconditions.checkState(
        !isAbstract() || !isFinal(), "abstract + final modifier combination is not allowed.");
    return this;
  }

  public Modifiers makeAbstract() {
    return toBuilder().makeAbstract().build();
  }

  public Modifiers makeStatic() {
    return toBuilder().makeStatic().build();
  }

  public Modifiers makeFinal() {
    return toBuilder().makeFinal().build();
  }

  abstract Builder toBuilder();

  public static Modifiers visibility(Visibility visibility) {
    return builder().setVisibility(visibility).build();
  }

  public static Builder builder() {
    return new AutoValue_Modifiers.Builder()
        .setVisibility(Visibility.PACKAGE_LOCAL)
        .setIsAbstract(false)
        .setIsStatic(false)
        .setIsFinal(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setVisibility(Visibility visibility);

    public abstract Builder setIsAbstract(boolean isAbstract);

    public Builder makeAbstract() {
      return setIsAbstract(true);
    }

    public abstract Builder setIsStatic(boolean isStatic);

    public Builder makeStatic() {
      return setIsStatic(true);
    }

    public abstract Builder setIsFinal(boolean isFinal);

    public Builder makeFinal() {
      return setIsFinal(true);
    }

    abstract Modifiers autoBuild();

    public Modifiers build() {
      return autoBuild().validate();
    }
  }
}
