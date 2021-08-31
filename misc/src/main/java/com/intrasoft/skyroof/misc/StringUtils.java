package com.intrasoft.skyroof.misc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtils {

    public static boolean isAscii(String text){
        return text.matches("\\A\\p{ASCII}*\\z");
    }

    public static String toJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
