/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <https://www.enginehub.org>
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

package org.enginehub.piston.inject;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.enginehub.piston.util.ValueProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@DisplayName("Values can be resolved in injected stores")
public class ValueResolutionTest {

    private static final Key<Object> O_KEY = Key.of(Object.class);
    private static final Key<String> S_KEY = Key.of(String.class);
    private static final Key<Integer> I_KEY = Key.of(Integer.class);

    @Test
    @DisplayName("- recursively, quickly, with high concurrency")
    void recursivelyResolvesQuickly() throws Exception {
        Object storedObj = new Object();
        String storedStr = storedObj.toString();
        Integer storedInt = storedStr.hashCode();
        InjectedValueStore primaryStore = MapBackedValueStore.create();
        primaryStore.injectValue(O_KEY,
            ValueProvider.constant(storedObj));
        primaryStore.injectValue(S_KEY,
            context -> context.injectedValue(O_KEY).map(Object::toString));
        primaryStore.injectValue(I_KEY,
            context -> context.injectedValue(S_KEY).map(Object::hashCode));

        MemoizingValueAccess secondaryAccess = MemoizingValueAccess.wrap(primaryStore);

        int count = 10000 * Runtime.getRuntime().availableProcessors();
        System.err.println("Launching " + count + " tasks.");
        ListeningExecutorService threads = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
        );
        List<ListenableFuture<?>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(threads.submit(() -> {
                assertTimeout(ofSeconds(5), () -> {
                    Optional<Integer> value = secondaryAccess.injectedValue(I_KEY);

                    assertEquals(Optional.of(storedInt), value);
                    InjectedValueAccess memory = secondaryAccess.snapshotMemory();
                    assertEquals(Optional.of(storedObj), memory.injectedValue(O_KEY));
                    assertEquals(Optional.of(storedStr), memory.injectedValue(S_KEY));
                    assertEquals(Optional.of(storedInt), memory.injectedValue(I_KEY));
                });

                return null;
            }));
        }

        System.err.println("Waiting for tasks to finish...");
        Futures.allAsList(futures).get(30, TimeUnit.SECONDS);
    }
}
