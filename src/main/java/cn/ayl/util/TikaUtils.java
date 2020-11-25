package cn.ayl.util;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * created by Rock-Ayl on 2020-11-5
 * apache tika Utils 文件内容识别、抽取工具
 */
public class TikaUtils {

    protected static Logger logger = LoggerFactory.getLogger(TikaUtils.class);

    /**
     * 抽取文件文本内容
     *
     * @param file 文件对象
     * @return
     */
    public static String getContext(File file) {
        try {
            //解析并返回
            new Tika().parseToString(file);
        } catch (Exception e) {
            logger.error("抽取文件[{}]失败,e:[{}]", file.getName(), e);
        }
        //缺省
        return "";
    }

}  