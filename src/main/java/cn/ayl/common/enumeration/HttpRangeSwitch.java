package cn.ayl.common.enumeration;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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

    /**
     * 根据Range value获取文件字节起始
     *
     * @return
     */
    public static long start(String value, File file) {
        //解析Range类型
        HttpRangeSwitch rangeSwitch = parse(value);
        //初始化
        long start;
        //根据类型和内容返回start位置
        switch (rangeSwitch) {
            case None:
            case Array:
                start = 0L;
                break;
            case Right:
                start = Long.parseLong(value.replaceAll("bytes=-", ""));
                break;
            case Left:
                start = Long.parseLong(value.replaceAll("bytes=", "").replaceAll("-", ""));
                break;
            case LeftAndRight:
            default:
                //去除前缀
                String leftAndRightValue = value.replaceAll("bytes=", "");
                //分割为List
                List<String> list = Arrays.asList(leftAndRightValue.split("-"));
                //获取start
                start = Long.parseLong(list.get(0));
                break;
        }
        return start;
    }

    /**
     * 根据Range value获取文件字节终止
     *
     * @return
     */
    public static long end(String value, File file) {
        //解析Range类型
        HttpRangeSwitch rangeSwitch = parse(value);
        //初始化
        long end;
        //根据类型和内容返回end位置
        switch (rangeSwitch) {
            case None:
            case Array:
                end = file.length();
                break;
            case Right:
                end = file.length();
                break;
            case Left:
                end = file.length();
                break;
            case LeftAndRight:
            default:
                //去除前缀
                String leftAndRightValue = value.replaceAll("bytes=", "");
                //分割为List
                List<String> list = Arrays.asList(leftAndRightValue.split("-"));
                //获取start
                end = Long.parseLong(list.get(1));
                break;
        }
        return end;
    }

}
