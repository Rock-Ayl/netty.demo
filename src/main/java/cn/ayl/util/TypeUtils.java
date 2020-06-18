package cn.ayl.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * created by Rock-Ayl on 2019-11-14
 * 类型工具类
 */
public class TypeUtils {

    /**
     * 根据文件名 返回 CONTENT_TYPE
     *
     * @param fileName 文件路径
     * @return
     */
    public static String parseHttpResponseContentType(String fileName) {
        //获取文件后缀
        String fileExt = FilenameUtils.getExtension(fileName);
        //判空
        if (StringUtils.isNotBlank(fileExt)) {
            //小写
            fileExt = fileExt.toLowerCase();
            //分发
            switch (fileExt) {
                case "txt":
                case "html":
                    return "text/html; charset=UTF-8";
                case "text":
                    return "text/plain; charset=UTF-8";
                case "json":
                    return "application/json; charset=UTF-8";
                case "css":
                    return "text/css; charset=UTF-8";
                case "js":
                    return "application/javascript;charset=utf-8";
                case "svg":
                    return "Image/svg+xml; charset=utf-8";
                case "jpeg":
                case "jpg":
                    return "image/jpeg";
                case "ico":
                    return "image/x-icon";
                case "png":
                    return "image/png";
                case "pdf":
                    return "application/pdf; charset=utf-8";
                case "gif":
                    return "image/gif";
                case "mp3":
                    return "audio/mp3; charset=utf-8";
                case "mp4":
                case "mkv":
                    return "video/mp4; charset=utf-8";
            }
        }
        //缺省
        return "application/octet-stream";
    }

    /**
     * 强转类为 任意类型
     *
     * @param clz 类型
     * @param o   被转换对象
     * @param <T>
     * @return
     */
    public static <T> T castObject(Class<T> clz, Object o) {
        if (null == o) {
            return null;
        }
        //fastJson的强转,值得信赖
        return JSON.parseObject(JSON.toJSONString(o), clz);
    }

}
