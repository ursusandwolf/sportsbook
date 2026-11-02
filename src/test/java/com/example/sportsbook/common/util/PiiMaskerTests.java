package com.example.sportsbook.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PiiMaskerTests {

    @Test
    void masksEmailLocalPart() {
        assertEquals("pla***@example.com", PiiMasker.mask("player@example.com"));
    }

    @Test
    void masksLongDigitSequences() {
        assertEquals("card 12****89", PiiMasker.mask("card 123456789"));
    }

    @Test
    void leavesNullUnchanged() {
        assertNull(PiiMasker.mask(null));
    }
}