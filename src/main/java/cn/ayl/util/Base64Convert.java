package cn.ayl.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * created by Rock-Ayl 2019-10-16
 * 操作base64的class
 */
public class Base64Convert {

    protected static Logger logger = LoggerFactory.getLogger(Base64Convert.class);

    /**
     * base64文本解码
     *
     * @param content
     * @return
     */
    public static String decode64(String content) {
        Base64 base64 = new Base64();
        try {
            byte[] bytes = base64.decode(content.getBytes("UTF-8"));
            content = new String(bytes);
            content = content.replaceAll("\r|\n", "");
        } catch (Exception e) {
        }
        return content;
    }

    /**
     * 文件转base64
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String fileToBase64(String fileName) throws IOException {
        //fileName源文件
        String strBase64 = null;
        try {
            InputStream in = new FileInputStream(fileName);
            // in.available()返回文件的字节长度
            byte[] bytes = new byte[in.available()];
            // 将文件中的内容读入到数组中
            in.read(bytes);
            strBase64 = new BASE64Encoder().encode(bytes);      //将字节流数组转换为字符串
            in.close();
        } catch (FileNotFoundException fe) {
            System.out.println("文件转换base64报错");
            fe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return strBase64;
    }

    /**
     * base64文件转成byte[]
     *
     * @param content
     * @return
     */
    public static byte[] resolveBase64(String content) {   //对字节数组字符串进行Base64解码并生成图片
        if (content == null) {
            return null;
        }
        //删除前缀、解码
        return decodeBASE64(delPrefix(content));
    }

    /**
     * base64文件内容转成文件
     *
     * @param content
     * @param filePath
     * @return
     */
    public static Boolean decryptByBase64(String content, String filePath) {
        if (content == null && filePath == null) {
            return false;
        }
        try {
            //内容转byte[]
            byte[] by = resolveBase64(content);
            //写入
            Files.write(Paths.get(filePath), by, StandardOpenOption.CREATE);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * base64文件内容解码成byte
     *
     * @param content
     * @return
     */
    private static byte[] decodeBASE64(String content) {
        if (content == null)
            return null;
        try {
            return Base64.decodeBase64(content);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将一个完整的base64文件前缀删除,方便转化
     *
     * @param base64str
     * @return
     */
    private static String delPrefix(String base64str) {
        return base64str.replace(base64str.substring(0, base64str.indexOf(",") + 1), "");
    }

}