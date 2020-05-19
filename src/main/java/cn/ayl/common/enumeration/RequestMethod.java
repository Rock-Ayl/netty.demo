package cn.ayl.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 请求方法
 */
public enum RequestMethod {

    Get,
    Post,
    Options;

    /**
     * 解析请求
     *
     * @param value
     * @return
     */
    public static RequestMethod parse(String value) {
        //判空
        if (StringUtils.isNotEmpty(value)) {
            for (RequestMethod requestMethod : RequestMethod.values()) {
                if (requestMethod.toString().toLowerCase().equals(value.toLowerCase())) {
                    return requestMethod;
                }
            }
        }
        return Get;
    }

}
