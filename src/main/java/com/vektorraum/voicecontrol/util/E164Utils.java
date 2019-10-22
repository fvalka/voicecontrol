package com.vektorraum.voicecontrol.util;

import java.util.regex.Pattern;

public class E164Utils {
    private static final Pattern e164Regex = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    public static boolean isValid(String s) {
        return e164Regex.matcher(s).matches();
    }
}
