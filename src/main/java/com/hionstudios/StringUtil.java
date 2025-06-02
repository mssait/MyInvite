package com.hionstudios;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hionstudios.time.TimeUtil;

public class StringUtil {
    public static boolean isEmail(String email) {
        return Pattern.compile("^[A-Z\\d._%+-]+@[A-Z\\d.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
                .matcher(email).find();
    }

    public static boolean isPostcode(String postcode) {
        return Pattern.compile("^[1-9]\\d{5}$").matcher(postcode).find();
    }

    public static boolean isPhone(String phone) {
        return Pattern.compile("^[6-9]\\d{9}$").matcher(phone).find();
    }

    public static boolean isNumber(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String string) {
        return Arrays.asList("true", "false").contains(string.toLowerCase());
    }

    public static boolean isTime(String time) {
        if (time.contains("T")) {
            String[] split = time.split("T");
            return TimeUtil.parse(split[0], "yyyy-MM-dd") > 0 && TimeUtil.parse(split[1], "HH:mm") > 0;
        }
        return false;
    }

    public static boolean isDate(String time) {
        try {
            return TimeUtil.parse(time, "yyyy-MM-dd") > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String nullify(String string) {
        if (string == null || string.equals("") || string.equals("null")) {
            return null;
        }
        return string;
    }

    public static String coalesce(String... params) {
        for (String param : params) {
            if (param != null) {
                return param;
            }
        }
        return "";
    }

    public static String seoEncode(String url) {
        return url != null ? url.replace(" ", "-").replace("(", "")
                .replace(")", "").replace("[", "").replace("]", "")
                .replace("&", "and").replace("?", "").replace("#", "")
                .replace("'", "") : "";
    }

    public static boolean isJsonObject(String string) {
        return string.charAt(0) == '{';
    }
}
