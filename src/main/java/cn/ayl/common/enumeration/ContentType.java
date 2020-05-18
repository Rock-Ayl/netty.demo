package cn.ayl.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 请求内容类型
 */
public enum ContentType {

    //一般资源类型
    None,
    Error,
    Text,
    Html,
    Css,
    Js,
    Jpeg,
    Jpg,
    Png,
    Gif,
    Ico,
    Mp3,
    Mp4,
    Data,
    //application/json
    Json,
    //application/x-www-form-urlencoded
    XWWWFormUrlencoded,
    //multipart/form-data
    MultipartFormData;

    /**
     * 根据value解析
     *
     * @return
     */
    public static ContentType parse(String value) {
        //判空
        if (StringUtils.isNotEmpty(value)) {
            //修剪下
            value = value.trim();
            if (value.contains("x-www-form-urlencoded")) {
                return XWWWFormUrlencoded;
            } else if (value.contains("form-data")) {
                return MultipartFormData;
            } else if (value.contains("application/json")) {
                return Json;
            }
        }
        //缺省
        return None;
    }


}
