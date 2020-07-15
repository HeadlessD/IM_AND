package com.qbao.newim.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenjian on 2017/4/13.
 */

public class SnsPattern {
    private static final String URL_REG = "((http(s)?://)?([\\w-]+\\.)+[a-zA-Z-]{2,}(:[0-9]{0,4})?(/[\\w-./?%&=]*)?)";
    private static final String SNS_URL_REG = "((http(s)?://)([\\w-]+\\.)+[a-zA-Z-]{2,}(:[0-9]{0,4})?(/[\\w-./?%&=]*)?)";
    private static final String PHONE_REG = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
    private static final Pattern mURLPattern = Pattern.compile(URL_REG, Pattern.CASE_INSENSITIVE);
    private static final Pattern mSNSURLPattern = Pattern.compile(SNS_URL_REG, Pattern.CASE_INSENSITIVE);
    private static final Pattern mPhonePattern = Pattern.compile(PHONE_REG);

    public static Matcher urlMatcher(String string) {
        return mURLPattern.matcher(string);
    }

    public static Matcher snsUrlMatcher(String string) {
        return mSNSURLPattern.matcher(string);
    }

    public static Matcher phoneMatcher(String string) {
        return mPhonePattern.matcher(string);
    }
}
