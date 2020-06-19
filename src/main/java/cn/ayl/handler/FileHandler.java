package cn.ayl.handler;

import cn.ayl.common.entry.FileEntry;
import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import cn.ayl.config.Const;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.List;

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
     * 处理上传
     *
     * @param fileEntryList 文件实体
     * @param params        其他参数对象
     * @return
     */
    public JsonObject uploadFile(List<FileEntry> fileEntryList, JsonObject params) {
        //初始化返回值
        JsonObjects items = JsonObjects.VOID();
        //判空
        if (CollectionUtils.isNotEmpty(fileEntryList)) {
            //循环
            for (FileEntry fileEntry : fileEntryList) {
                //todo 业务逻辑
                //组装参数
                JsonObject fileObject = fileEntry.toJson();
                fileObject.putAll(params);
                items.add(fileObject);
            }
        }
        return JsonObject.Success().append(Const.Items, items);
    }

}