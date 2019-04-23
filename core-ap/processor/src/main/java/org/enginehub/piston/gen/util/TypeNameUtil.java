package org.enginehub.piston.gen.util;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class TypeNameUtil {

    public static TypeName rawType(TypeName typeName) {
        if (typeName instanceof ClassName) {
            return typeName;
        } else if (typeName instanceof ArrayTypeName) {
            return ArrayTypeName.of(rawType(((ArrayTypeName) typeName).componentType));
        } else if (typeName instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) typeName).rawType;
        } else if (typeName.isPrimitive()) {
            return typeName;
        }
        throw new IllegalArgumentException("Not able to create a raw type from " + typeName.getClass());
    }

    public static TypeName firstTypeArg(TypeName typeName) {
        if (typeName instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) typeName).typeArguments.get(0);
        }
        return TypeName.OBJECT;
    }

    private TypeNameUtil() {
    }

}
