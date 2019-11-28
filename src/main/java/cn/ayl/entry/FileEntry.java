package cn.ayl.entry;

import cn.ayl.util.json.JsonObject;

/**
 * created by Rock-Ayl 2019-
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
    private int fileSize = 0;
    //文件其他额外对象
    private JsonObject fileObject;

    public JsonObject toJson() {
        JsonObject result = JsonObject.VOID();
        result.append("fileId", getFileId());
        result.append("fileName", getFileName());
        result.append("fileExt", getFileExt());
        result.append("filePath", getFilePath());
        result.append("fileSize", getFileSize());
        result.append("fileObject", getFileObject());
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

    public int getFileSize() {
        return fileSize;
    }

    public JsonObject getFileObject() {
        return fileObject;
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

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileObject(JsonObject fileObject) {
        this.fileObject = fileObject;
    }
}
