package cn.ayl.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * created by Rock-Ayl 2019-10-16
 * 操作base64的class
 */
public class Base64Utils {

    protected static Logger logger = LoggerFactory.getLogger(Base64Utils.class);

    /**
     * base64文本解码
     *
     * @param content
     * @return
     */
    public static String decode64(String content) {
        try {
            content = new String(new Base64().decode(content.getBytes("UTF-8")));
            content = content.replaceAll("\r|\n", "");
        } catch (Exception e) {
            logger.error("解码失败.");
            return null;
        }
        return content;
    }

    /**
     * 文件转base64
     *
     * @param filePath 文件path
     * @return base64码
     */
    public static String toBase64(String filePath) {
        //fileName源文件
        String strBase64;
        try {
            InputStream in = new FileInputStream(filePath);
            // in.available()返回文件的字节长度
            byte[] bytes = new byte[in.available()];
            // 将文件中的内容读入到数组中
            in.read(bytes);
            //将字节流数组转换为字符串
            strBase64 = new BASE64Encoder().encode(bytes);
            in.close();
        } catch (IOException e) {
            logger.error("文件转base64出现异常: {}", e);
            return null;
        }
        return strBase64;
    }

    /**
     * base64文件转成byte[]
     *
     * @param base64String
     * @return
     */
    public static byte[] toByteList(String base64String) {
        if (base64String == null) {
            return null;
        }
        //删除前缀、解码
        return Base64.decodeBase64(delPrefix(base64String));
    }

    /**
     * base64内容转成文件
     *
     * @param base64String
     * @param filePath
     * @return
     */
    public static Boolean toFile(String base64String, String filePath) {
        if (base64String == null && filePath == null) {
            return false;
        }
        try {
            //内容转byte[]
            byte[] by = toByteList(base64String);
            //写入
            Files.write(Paths.get(filePath), by, StandardOpenOption.CREATE);
        } catch (IOException e) {
            logger.error("base64内容转成文件出现异常: {}", e);
            return false;
        }
        return true;
    }

    /**
     * 将一个base64文件的前缀删除,方便转化
     *
     * @param base64String
     * @return
     */
    public static String delPrefix(String base64String) {
        return base64String.replace(base64String.substring(0, base64String.indexOf(",") + 1), "");
    }

}