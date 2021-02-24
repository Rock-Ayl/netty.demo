package cn.ayl.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 服务支持的请求方法类型
 */
public enum RequestMethod {

    Get,
    Post,
    Put,
    Delete,
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
