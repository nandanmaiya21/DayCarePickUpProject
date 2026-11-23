package com.example.daycarepickupproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.daycarepickupproject.Models.NameUtils;
import com.example.daycarepickupproject.Models.PinHasher;
import com.example.daycarepickupproject.Models.PinModel;

import org.junit.Test;

public class ModelsTest {

    @Test
    public void pinModelStopsAtFourDigits() {
        PinModel pin = new PinModel();
        for (String d : new String[]{"1", "2", "3", "4"}) {
            assertTrue(pin.addDigit(d));
        }
        assertFalse(pin.addDigit("5"));
        assertTrue(pin.isComplete());
        assertEquals("1234", pin.getPin());
        pin.removeLastDigit();
        assertEquals("123", pin.getPin());
        pin.clear();
        assertEquals(0, pin.getLength());
    }

    @Test
    public void pinHashIsStableAndNotThePin() {
        assertEquals(PinHasher.hash("1234"), PinHasher.hash("1234"));
        assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("1235"));
        assertFalse(PinHasher.hash("1234").contains("1234"));
    }

    @Test
    public void initials() {
        assertEquals("AO", NameUtils.initials("Ava Okafor"));
        assertEquals("A", NameUtils.initials("Ava"));
        assertEquals("MA", NameUtils.initials("  mary   ann smith "));
        assertEquals("?", NameUtils.initials(""));
        assertEquals("Ava", NameUtils.firstName("Ava Okafor"));
    }
}
