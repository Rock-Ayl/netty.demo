package cn.ayl.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 文件的请求类型
 */
public enum FileRequestType {

    //预览文件
    preview,
    //下载文件
    download;

    /**
     * 解析文件的请求类型
     *
     * @param value
     * @return
     */
    public static FileRequestType parse(String value) {
        //判空
        if (StringUtils.isNotEmpty(value)) {
            //判空
            for (FileRequestType fileRequestType : FileRequestType.values()) {
                if (fileRequestType.toString().toLowerCase().equals(value.toLowerCase())) {
                    return fileRequestType;
                }
            }
        }
        return FileRequestType.preview;
    }

}
