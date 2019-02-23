package com.enginehub.piston.impl;

import com.enginehub.piston.CommandManager;
import com.enginehub.piston.CommandManagerService;
import com.google.auto.service.AutoService;

@AutoService(CommandManagerService.class)
public class CommandManagerServiceImpl implements CommandManagerService {
    @Override
    public String id() {
        return "default-impl";
    }

    @Override
    public CommandManager newCommandManager() {
        return new CommandManagerImpl();
    }
}
