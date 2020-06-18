package cn.ayl.common.entry;

import cn.ayl.config.Const;
import cn.ayl.common.json.JsonObject;

/**
 * created by Rock-Ayl 2019-12-12
 * 一个文件实体
 */
public class FileEntry {

    //文件id
    private String fileId;
    //文件名
    private String fileName;
    //文件后缀
    private String fileExt;
    //文件当前实体在服务器下路径
    private String filePath;
    //文件大小
    private long fileSize = 0L;

    public JsonObject toJson() {
        JsonObject result = JsonObject.VOID();
        result.append(Const.FileId, getFileId());
        result.append(Const.FileName, getFileName());
        result.append(Const.FileExt, getFileExt());
        result.append(Const.FilePath, getFilePath());
        result.append(Const.FileSize, getFileSize());
        return result;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExt() {
        return fileExt;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

}
