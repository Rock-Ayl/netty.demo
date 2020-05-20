package cn.ayl.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * Created By Rock-Ayl on 2020-05-20
 * http header Range 的几种选择
 */
public enum HttpRangeSwitch {

    //下载0-最大 eg bytes=0-,
    None,
    //下载200-最大 eg:bytes=200-
    Left,
    //下载27000-39000 eg:bytes=27000-39000
    LeftAndRight,
    //下载最后的39000个字节 eg:bytes=-39000
    Right,
    //同时指定区间内容：bytes=500-600,601-999
    Array;

    public static final String head = "bytes=";

    /**
     * 解析Range
     *
     * @return
     */
    public static HttpRangeSwitch parse(String value) {
        //判空
        if (StringUtils.isEmpty(value)) {
            return None;
        }
        //如果存在,
        if (value.contains(",")) {
            return Array;
        }
        //如果存在=-
        if (value.contains("=-")) {
            return Right;
        }
        //如果以-结尾
        if (value.endsWith("-")) {
            return Left;
        }
        //缺省普通区间
        return LeftAndRight;
    }

}
