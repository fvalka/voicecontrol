package com.vektorraum.voicecontrol;

import com.vektorraum.voicecontrol.util.E164Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class E164UtilsTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"+4369923429132", true},
                {"4359228491838", true},
                {"+123456789012345", true},
                {"+1234", true},
                {"+1234567890123456", false},
                {"+1234567812 56", false},
                {"*123456781246", false},
                {"+12345678asd", false},
                {"+12345678\n334", false}
        });
    }

    private String number;
    private boolean valid;

    public E164UtilsTest(String number, boolean valid) {
        this.number = number;
        this.valid = valid;
    }

    @Test
    public void test() {
        boolean result = E164Utils.isValid(number);
        assertEquals(valid, result);
    }
}