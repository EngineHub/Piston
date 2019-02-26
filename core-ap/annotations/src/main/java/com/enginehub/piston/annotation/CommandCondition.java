package com.enginehub.piston.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a condition that must be satisfied before the command
 * executes. This may be used as a meta-annotation, to allow it to be combined with a custom
 * annotation that has additional parameters.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface CommandCondition {

    /**
     * The type to use to generate the actual condition.
     */
    Class<? extends CommandConditionGenerator> type();

}
