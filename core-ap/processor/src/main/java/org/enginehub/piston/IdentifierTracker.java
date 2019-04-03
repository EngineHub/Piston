package org.enginehub.piston;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.enginehub.piston.util.SafeName;
import org.enginehub.piston.value.ReservedNames;

public class IdentifierTracker {

    private static String realName(Multiset<String> memory, String name) {
        // Make the name safe first
        name = SafeName.from(name);
        memory.add(name);
        int count = memory.count(name);
        return count == 1 ? name : name + count;
    }

    private final Multiset<String> fieldNames = HashMultiset.create(ReservedNames.fieldNames());
    private final Multiset<String> methodNames = HashMultiset.create(ReservedNames.methodNames());

    public String fieldName(String requested) {
        return realName(fieldNames, requested);
    }

    public String methodName(String requested) {
        return realName(methodNames, requested);
    }
}
