package org.enginehub.piston.util;

import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RegistrationUtil")
public class RegistrationUtilTest {

    @Test
    void requireOptional() {
        assertEquals("x",
            RegistrationUtil.requireOptional(Key.of(String.class), "name", Optional.of("x")));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            RegistrationUtil.requireOptional(Key.of(String.class), "name", Optional.empty())
        );
        assertTrue(ex.getMessage().contains("name"));
        assertTrue(ex.getMessage().contains(Key.of(String.class).toString()));
    }

}
