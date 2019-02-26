package com.enginehub.piston.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method with this to mark it as a command. It will have help,
 * argument converters, and completions automatically generated for it.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String name();

    /**
     * Aliases for this command, besides the {@linkplain #name() main name}.
     */
    String[] aliases() default {};

    /**
     * A description of the command.
     */
    String desc();

    /**
     * Add some text to the end of the help message.
     *
     * <p>
     * This is useful for warnings and extra information.
     * </p>
     */
    String descFooter() default "";

}
