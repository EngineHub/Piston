package com.enginehub.piston.annotation;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

public interface CommandConditionGenerator {

    /**
     * Bake the parameters given by the annotation into a fast runtime check.
     *
     * <p>
     * This method is called during annotation processing.
     * </p>
     *
     * @param condition the annotation that defines the check. The type of the mirror is
     *     guaranteed to be {@link CommandCondition}.
     * @param enclosingMethod the method that the condition was found on. This is useful for
     *     gathering information via custom annotations
     * @param depSupport variable dependency support, if you need an object instance request
     *     it via this interface
     * @return a block of code representing a method that returns a boolean
     */
    CodeBlock generateCondition(AnnotationMirror condition,
                                ExecutableElement enclosingMethod,
                                DependencySupport depSupport);

}
