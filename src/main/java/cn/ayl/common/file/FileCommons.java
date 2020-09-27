package cn.ayl.common.file;

import cn.ayl.common.db.jdbc.SqlTable;
import cn.ayl.common.entry.FileEntry;

/**
 * Created By Rock-Ayl on 2020-09-27
 * 文件业务的共有逻辑
 */
public class FileCommons {

    /**
     * 插入文件信息至Mysql
     *
     * @param fileEntry
     */
    public static void insertFileInfoToMySql(FileEntry fileEntry) {
        //文件信息记录至Mysql
        SqlTable.use().insert("INSERT file (fileId,fileName,fileSize,fileMD5,fileUploadTime) VALUES (?,?,?,?,?)", new Object[]{fileEntry.getFileId(), fileEntry.getFileName(), fileEntry.getFileSize(), fileEntry.getFileMD5(), System.currentTimeMillis()});
    }

}
