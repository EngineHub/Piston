package org.enginehub.piston.gen

import org.enginehub.piston.Command
import org.enginehub.piston.annotation.CommandCondition
import java.lang.reflect.Method

class AlwaysTrueConditionGenerator : CommandConditionGenerator {
    override fun generateCondition(commandMethod: Method): Command.Condition {
        return Command.Condition.TRUE
    }
}

@CommandCondition(AlwaysTrueConditionGenerator::class)
annotation class AlwaysTrueCondition
