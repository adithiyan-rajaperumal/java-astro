package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.util.BaminiConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaminiConverterTest {

    @Test
    public void testTamilDiAndDeeConversion() {
        assertEquals("b", BaminiConverter.convert("டி"));
        assertEquals("B", BaminiConverter.convert("டீ"));
        assertEquals("KbT", BaminiConverter.convert("முடிவு"));
        assertEquals("nghUj;j KbT", BaminiConverter.convert("பொருத்த முடிவு"));
    }
}
