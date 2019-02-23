package com.enginehub.piston;

import com.enginehub.piston.converter.SimpleArgumentConverter;
import com.enginehub.piston.part.ArgAcceptingCommandFlag;
import com.enginehub.piston.part.CommandArgument;
import com.enginehub.piston.part.CommandFlag;
import com.google.common.collect.ImmutableList;

import static com.enginehub.piston.part.CommandParts.arg;
import static com.enginehub.piston.part.CommandParts.flag;

public class ClipboardCommands {

    private static final CommandArgument<Object> LEAVE_ID =
            arg("leaveId", "Leaves this block in place of removed blocks")
                    .convertedBy(SimpleArgumentConverter.fromSingle(x -> (Object) x, "pattern"))
                    .defaultsTo(ImmutableList.of("air"))
                    .build();
    private static final CommandFlag COPY_ENTITIES =
            flag('e', "Also copy entities").build();
    private static final CommandFlag CUT_ENTITIES =
            flag('e', "Also cut entities").build();
    private static final ArgAcceptingCommandFlag<Object> MASK =
            flag('m', "Add a source mask, excluded blocks become air in the paste")
                    .withRequiredArg()
                    .convertedBy(SimpleArgumentConverter.fromSingle(x -> (Object) x, "mask"))
                    .build();

    public static void main(String[] args) {
        CommandManager manager = DefaultCommandManangerService.getInstance().newCommandManager();
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
        Object mask = MASK.value(params);

        return 1;
    }

    private int cut(CommandParameters params) {
        boolean cutEntities = CUT_ENTITIES.in(params);
        Object mask = MASK.value(params);
        Object leavePattern = LEAVE_ID.value(params);

        return 1;
    }

}
