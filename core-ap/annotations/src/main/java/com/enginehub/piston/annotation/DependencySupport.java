package com.enginehub.piston.annotation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

public interface DependencySupport {

    /**
     * Request that a variable with the given type and name be in-scope
     * for the code block returned by the method using this support object.
     *
     * <p>
     * The annotations are added the parameters wherever they are added,
     * usually in the constructor. This allows for the use of {@code @Provided} or similar
     * injection mechanisms.
     * </p>
     *
     * @param type the type of the variable
     * @param name the base name of the variable
     * @param annotations the annotations to add to any parameter
     * @return the actual name of the variable. You must use this to reference it.
     */
    String requestInScope(TypeName type, String name, AnnotationSpec... annotations);

}
