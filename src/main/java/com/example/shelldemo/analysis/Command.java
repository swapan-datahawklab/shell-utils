package com.example.shelldemo.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Annotation to mark a class as a command.
 * Commands must implement the Runnable interface and provide a no-args constructor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command {
    /**
     * The name of the command. Must be unique across all commands.
     * @return the command name
     */
    String name();

    /**
     * A brief description of what the command does.
     * @return the command description
     */
    String description();

    /**
     * Usage instructions for the command.
     * @return the usage instructions
     */
    String usage();

    /**
     * Whether the command should be hidden from help and listing.
     * @return true if the command should be hidden
     */
    boolean hidden() default false;

    /**
     * The minimum number of arguments required by the command.
     * @return the minimum number of arguments
     */
    int minArgs() default 0;

    /**
     * The maximum number of arguments allowed by the command.
     * Use -1 for unlimited arguments.
     * @return the maximum number of arguments
     */
    int maxArgs() default -1;
}