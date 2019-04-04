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

import com.google.common.collect.ImmutableList;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;

import static org.enginehub.piston.part.CommandParts.arg;
import static org.enginehub.piston.part.CommandParts.flag;

public class ClipboardCommands {

    private static final CommandArgument LEAVE_ID =
        arg("leaveId", "Leaves this block in place of removed blocks")
            .defaultsTo(ImmutableList.of("air"))
            .build();
    private static final CommandFlag COPY_ENTITIES =
        flag('e', "Also copy entities").build();
    private static final CommandFlag CUT_ENTITIES =
        flag('e', "Also cut entities").build();
    private static final ArgAcceptingCommandFlag MASK =
        flag('m', "Add a source mask, excluded blocks become air in the paste")
            .withRequiredArg()
            .build();

    public static void main(String[] args) {
        CommandManager manager = DefaultCommandManagerService.getInstance().newCommandManager();
        new ClipboardCommands().register(manager);
        manager.getAllCommands().forEach(c -> System.err.println(c.getFullHelp()));
    }

    public void register(CommandManager manager) {
        manager.register("/copy", cmd ->
            cmd.description("Copy the selection to the clipboard")
                .footer("WARNING: Pasting entities cannot yet be undone!")
                .addParts(COPY_ENTITIES, MASK)
                .action(this::copy));
        manager.register("/cut", cmd ->
            cmd.description("Cut the selection to the clipboard")
                .footer("WARNING: Cutting and pasting entities cannot yet be undone!")
                .addParts(CUT_ENTITIES, MASK, LEAVE_ID)
                .action(this::cut));
    }

    private int copy(CommandParameters params) {
        boolean copyEntities = COPY_ENTITIES.in(params);
        Object mask = MASK.value(params).asSingle(Object.class);

        return 1;
    }

    private int cut(CommandParameters params) {
        boolean cutEntities = CUT_ENTITIES.in(params);
        Object mask = MASK.value(params).asSingle(Object.class);
        Object leavePattern = LEAVE_ID.value(params).asSingle(Object.class);

        return 1;
    }

}
