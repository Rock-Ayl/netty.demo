package cn.ayl.handler;

import cn.ayl.common.entry.FileEntry;
import cn.ayl.common.json.JsonObject;
import cn.ayl.config.Const;

import java.io.File;

/**
 * create by Rock-Ayl 2020/1/16
 * 文件 业务处理器
 */
public enum FileHandler {

    instance;

    /**
     * todo 读取下载流逻辑,现在没有业务，随意写了一个，以后可以添加身份
     * 读取业务中的文件
     *
     * @param type
     * @param fileId
     * @param fileName
     * @return
     */
    public File readDownloadFile(String type, String fileId, String fileName) {
        File file = new File(Const.DownloadFilePath + fileName);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * todo 读取服务器静态资源
     *
     * @param pathSuffix 资源路径后缀
     * @return
     */
    public File readResourceFile(String pathSuffix) {
        return new File(Const.ResourcePath + pathSuffix);
    }

    /**
     * todo 文件上传业务处理
     *
     * @param fileEntry 文件实体
     * @return
     */
    public JsonObject uploadFile(FileEntry fileEntry) {
        return JsonObject.Success().append(Const.Data, fileEntry.toJson());
    }

}
