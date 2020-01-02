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

package eh;


import com.google.common.collect.ImmutableList;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.gen.EmptySuperClass;
import org.enginehub.piston.gen.EmptySuperInterface;

import java.util.Collection;

final class SuperTypeRegistration extends EmptySuperClass implements CommandRegistration<SuperType>, EmptySuperInterface {
    private CommandManager commandManager;

    private SuperType containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private SuperTypeRegistration() {
        this.listeners = ImmutableList.of();
    }

    static SuperTypeRegistration builder() {
        return new SuperTypeRegistration();
    }

    public SuperTypeRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public SuperTypeRegistration containerInstance(SuperType containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public SuperTypeRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
    }

}
