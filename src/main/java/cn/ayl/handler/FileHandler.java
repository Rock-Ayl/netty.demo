package cn.ayl.handler;

import cn.ayl.common.entry.FileEntry;
import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.common.file.FileCommons;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import cn.ayl.config.Const;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

/**
 * create by Rock-Ayl 2020/1/16
 * 文件业务处理器
 */
public enum FileHandler {

    instance;

    /**
     * 读取业务中的文件
     *
     * @param type     请求文件的类型
     * @param fileId   文件fileId
     * @param fileName 文件名
     * @param cookieId 用户cookieId,用来验证身份
     * @return
     */
    public File readDownloadFile(FileRequestType type, String fileId, String fileName, String cookieId) {
        //根据下载类型来进行处理
        switch (type) {
            //如果是下载、预览
            case download:
            case preview:
                //查询文件信息
                JsonObject fileInfo = FileCommons.readFileInfo(fileId);
                //判空
                if (fileInfo != null) {
                    //获取MD5
                    String fileMD5 = fileInfo.getString("fileMD5", "");
                    //获取MD5
                    long fileSize = fileInfo.getLong("fileSize", 0L);
                    //判空
                    if (StringUtils.isNotBlank(fileMD5) && fileSize > 0L) {
                        //从已经上传至系统的目录获取文件
                        File file = new File(FileCommons.initFilePath(fileMD5, fileSize));
                        //如果存在并且是个文件
                        if (file.exists() && file.isFile()) {
                            //返回
                            return file;
                        }
                    }
                }
                break;
        }
        //缺省
        return null;
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
                //获取文件fileId
                String fileId = fileEntry.getFileId();
                //文件实体转Json
                JsonObject fileObject = fileEntry.toJson();
                //将传过来的额外参数组装进实体
                fileObject.putAll(params);
                //文件信息记录至Mysql
                FileCommons.insertFileInfo(fileEntry);
                //文件信息记录至ES
                FileCommons.addFileIndexToES(fileId);
                //组装返回值
                items.add(fileObject);
            }
        }
        return JsonObject.Success().append(Const.Items, items);
    }

}