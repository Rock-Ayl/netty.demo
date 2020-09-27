package cn.ayl.common.file;

import cn.ayl.common.db.elaticsearch.IndexTable;
import cn.ayl.common.db.jdbc.SqlTable;
import cn.ayl.common.entry.FileEntry;
import cn.ayl.common.json.JsonObject;

/**
 * Created By Rock-Ayl on 2020-09-27
 * 文件业务的共有逻辑
 */
public class FileCommons {

    /**
     * 获取文件信息
     *
     * @param fileId
     * @return
     */
    public static JsonObject readFileInfo(String fileId) {
        //查询并返回
        return SqlTable.use().queryObject("SELECT * FROM file WHERE fileId = ?", new Object[]{fileId});
    }

    /**
     * 新增文件信息
     *
     * @param fileEntry
     */
    public static void insertFileInfo(FileEntry fileEntry) {
        //插入文件信息
        SqlTable.use().insert("INSERT file (fileId,fileName,fileSize,fileMD5,fileUploadTime) VALUES (?,?,?,?,?)", new Object[]{fileEntry.getFileId(), fileEntry.getFileName(), fileEntry.getFileSize(), fileEntry.getFileMD5(), System.currentTimeMillis()});
    }

    /**
     * 新增文件信息至ES
     *
     * @param fileId
     */
    public static void addFileIndexToES(String fileId) {
        //获取文件信息
        JsonObject fileInfo = readFileInfo(fileId);
        //判空
        if (fileInfo != null) {
            //插入至ES
            IndexTable.addIndexData(fileInfo);
        }
    }

}
