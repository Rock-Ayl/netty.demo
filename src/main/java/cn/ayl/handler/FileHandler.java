package cn.ayl.handler;

import cn.ayl.common.entry.FileEntry;
import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.common.json.JsonObject;
import cn.ayl.config.Const;

import java.io.File;

/**
 * create by Rock-Ayl 2020/1/16
 * 文件业务处理器
 */
public enum FileHandler {

    instance;

    /**
     * todo 读取下载流逻辑,现在没有业务,随意写了一个
     * 读取业务中的文件
     *
     * @param type     请求文件的类型
     * @param fileId   文件fileId
     * @param fileName 文件名
     * @param cookieId 用户cookieId,用来验证身份
     * @return
     */
    public File readDownloadFile(FileRequestType type, String fileId, String fileName, String cookieId) {
        File file = new File(Const.DownloadFilePath + fileName);
        //如果存在并且是个文件
        if (file.exists() && file.isFile()) {
            //返回
            return file;
        } else {
            //返回null
            return null;
        }
    }

    /**
     * 读取服务器静态资源
     *
     * @param pathSuffix 资源路径后缀
     * @return
     */
    public File readResourceFile(String pathSuffix) {
        return new File(Const.ResourceFilePath + pathSuffix);
    }

    /**
     * todo 文件上传业务处理
     *
     * @param fileEntry 文件实体
     * @param params    其他参数对象
     * @return
     */
    public JsonObject uploadFile(FileEntry fileEntry, JsonObject params) {
        return JsonObject.Success().append(Const.Data, fileEntry.toJson());
    }

}