package cn.ayl.pojo;

/**
 * created by Rock-Ayl 2019-12-12
 * 单个文件实体
 */
public class File {

    //文件id
    private String fileId;
    //文件名
    private String fileName;
    //文件后缀
    private String fileExt;
    //文件path
    private String filePath;
    //文件大小
    private long fileSize = 0L;

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    //文件MD5值
    public String fileMD5;

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
