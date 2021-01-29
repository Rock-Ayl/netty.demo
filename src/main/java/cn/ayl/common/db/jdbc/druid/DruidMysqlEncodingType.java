package cn.ayl.common.db.jdbc.druid;

import org.apache.commons.lang3.StringUtils;

/**
 * Created By Rock-Ayl on 2020-12-23
 * Mysql连接编码类型
 */
public enum DruidMysqlEncodingType {

    Auto("UTF-8"),
    UTF8("UTF-8"),
    UTF16("UTF-16"),
    GBK("GBK"),
    BIG5("BIG5"),
    ISO88591("ISO-8859-1"),
    EUC_JP("EUC_JP"),
    EUC_KR("EUC_KR"),
    CP850("CP850");

    //对应中文
    private final String Chinese;

    /**
     * 重写
     *
     * @param value 枚举对应中文
     */
    DruidMysqlEncodingType(String value) {
        this.Chinese = value;
    }

    /**
     * 解析
     *
     * @return
     */
    public static DruidMysqlEncodingType parse(String value) {
        //判空
        if (StringUtils.isNotEmpty(value)) {
            //循环
            for (DruidMysqlEncodingType type : DruidMysqlEncodingType.values()) {
                //是否相同
                if (type.toString().toLowerCase().equals(value.toLowerCase())) {
                    //返回
                    return type;
                }
            }
        }
        //默认
        return Auto;
    }

    /**
     * 解析
     *
     * @param value
     * @return
     */
    public static DruidMysqlEncodingType parse(Long value) {
        //缺省
        if (value != null) {
            //使用int
            return parse(value.intValue());
        }
        //默认
        return Auto;
    }

    /**
     * 解析
     *
     * @return
     */
    public static DruidMysqlEncodingType parse(Integer value) {
        //判空
        if (value != null) {
            //获取枚举组
            DruidMysqlEncodingType[] values = DruidMysqlEncodingType.values();
            //越界
            if (values.length > value) {
                //返回
                return values[value];
            }
        }
        //默认
        return Auto;
    }

    /**
     * 返回中文
     *
     * @return
     */
    public String toChinese() {
        return this.Chinese;
    }

}
