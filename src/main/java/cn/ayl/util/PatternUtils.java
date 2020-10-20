package cn.ayl.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * created by Rock-Ayl on 2020-4-2
 * 正则验证工具类
 */
public class PatternUtils {

    //验证用户名
    private final static String USER_NAME = "^(?!_)(?!.*?_$)[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";
    //验证密码
    private final static String Pwd = "^[\\w_]{6,20}$";
    //验证邮箱
    private static final String EMAIL_REGEX = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
    //验证是否是汉字
    private static final String CHINESE_REGEX = "^[\u4E00-\u9FA5]+$";
    //验证手机号（支持国际格式,中国内地，中国香港）
    private static final String MOBILE_REGEX = "(\\+\\d+)?1[34578]\\d{9}$";
    //验证固定电话号
    private static final String PHONE_REGEX = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
    //验证是否为URL
    private static final String URL_REGEX = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?";
    //验证身份证号码,居民身份证号码15位或18位，最后一位可能是数字或字母
    private static final String ID_CARD_REGEX = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";

    /**
     * 密码验证
     *
     * @param pwd
     * @return
     */
    public static boolean isPwd(String pwd) {
        if (StringUtils.isBlank(pwd)) {
            return false;
        }
        return Pattern.matches(Pwd, pwd);
    }

    /**
     * 验证中文
     */
    public static boolean isChinese(String chinese) {
        if (StringUtils.isBlank(chinese)) {
            return false;
        }
        return Pattern.matches(CHINESE_REGEX, chinese);
    }

    /**
     * 验证用户名
     */
    public static boolean isUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }
        return Pattern.matches(USER_NAME, userName);
    }

    /**
     * 验证邮箱
     */
    public static boolean isEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return Pattern.matches(EMAIL_REGEX, email);
    }

    /**
     * 验证身份证
     */
    public static boolean isIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        return Pattern.matches(ID_CARD_REGEX, idCard);
    }

    /**
     * 验证手机号码
     */
    public static boolean isMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }
        return Pattern.matches(MOBILE_REGEX, mobile);
    }

    /**
     * 验证固定电话号码
     */
    public static boolean isPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        return Pattern.matches(PHONE_REGEX, phone);
    }

    /**
     * 验证URL地址
     */
    public static boolean isURL(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return Pattern.matches(URL_REGEX, url);
    }

}
