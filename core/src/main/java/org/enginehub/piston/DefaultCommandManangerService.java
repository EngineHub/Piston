/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) Piston contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.piston;

import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Streams.stream;

/**
 * A {@link CommandManagerService} that delegates to a default manager.
 *
 * <p>
 * The default manager will be the first manager that appears in the {@link ServiceLoader}.
 * It can be changed at any point by simply setting it, though this will fail if any of the
 * delegating methods have been accessed.
 * </p>
 */
public class DefaultCommandManangerService implements CommandManagerService {

    private static final DefaultCommandManangerService INSTANCE = new DefaultCommandManangerService(getDefaultService());

    public static DefaultCommandManangerService getInstance() {
        return INSTANCE;
    }

    private static CommandManagerService getDefaultService() {
        ServiceLoader<CommandManagerService> loader = ServiceLoader.load(CommandManagerService.class);
        return stream(loader.iterator())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No default service available."));
    }

    private final Lock sealLock = new ReentrantLock();
    private CommandManagerService defaultService;
    private CommandManagerService sealedDefaultService;
    private boolean sealed;

    private DefaultCommandManangerService(CommandManagerService defaultService) {
        this.defaultService = defaultService;
    }

    private CommandManagerService sealDelegate() {
        if (!sealed) {
            sealLock.lock();
            try {
                // double-check under lock, it may have been sealed by another thread
                if (!sealed) {
                    sealed = true;
                    // capture this under the lock to ensure thread-safety
                    // once done, it will forever be the same value
                    sealedDefaultService = defaultService;
                }
            } finally {
                sealLock.unlock();
            }
        }
        // this can be done out of the lock, since the seal already
        // makes this effectively final -- it will never be changed again
        return sealedDefaultService;
    }

    public void setDefaultService(CommandManagerService defaultService) {
        sealLock.lock();
        try {
            checkState(!sealed, "Piston default service is sealed");
            this.defaultService = defaultService;
        } finally {
            sealLock.unlock();
        }
    }

    @Override
    public String id() {
        return sealDelegate().id();
    }

    @Override
    public CommandManager newCommandManager() {
        return sealDelegate().newCommandManager();
    }
}
