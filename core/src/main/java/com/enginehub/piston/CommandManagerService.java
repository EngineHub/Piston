package com.enginehub.piston;

/**
 * Service for providing {@link CommandManager}s.
 *
 * <p>
 * If registered as specified in {@link java.util.ServiceLoader}, {@link
 * DefaultCommandManangerService} will automatically
 * load the service and use it for providing {@link CommandManager}s.
 * </p>
 */
public interface CommandManagerService {

    /**
     * The unique ID for this service. Typically the name of the implementation.
     */
    String id();

    /**
     * Creates a new command manager.
     */
    CommandManager newCommandManager();

}
