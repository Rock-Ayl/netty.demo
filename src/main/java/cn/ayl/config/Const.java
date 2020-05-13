package cn.ayl.config;

import cn.ayl.socket.rpc.Context;
import cn.ayl.util.PropertyUtils;
import cn.ayl.common.json.JsonObject;
import io.netty.util.AttributeKey;


import java.util.ArrayList;
import java.util.List;

/**
 * created by Rock-Ayl 2019-11-5
 * 常量
 */
public class Const {

    /**
     * netty channel的上下文 AttributeKey
     */
    public static AttributeKey<Context> AttrContext = AttributeKey.valueOf("Context");

    /**
     * 配置文件对象
     */
    public static PropertyUtils properties = new PropertyUtils().use("setting.properties");

    /**
     * 常用字段
     */
    public static String SPACE = " ";
    public static String Message = "message";
    public static String Items = "items";
    public static String Data = "data";
    public static String IsSuccess = "isSuccess";
    public static String TotalCount = "totalCount";
    public static String Type = "type";
    public static String FileId = "fileId";
    public static String FileCreateTime = "fileCreateTime";
    public static String FileUpdateTime = "FileUpdateTime";
    public static String FileName = "fileName";
    public static String FileExt = "fileExt";
    public static String FilePath = "filePath";
    public static String FileSize = "fileSize";
    public static String FileObject = "fileObject";

    /**
     * 初始化除了Service外的其他几个服务
     */
    //WebSocket地址
    public static String WebSocketPath;
    //上传地址
    public static String UploadPath;
    //下载地址
    public static String DownloadPath;
    //HttpPage地址
    public static String HttpPagePath;
    //默认的服务初始化
    public static List<String> DefaultService;

    static {
        //WebSocket地址
        WebSocketPath = "/WebSocket";
        //上传地址
        UploadPath = "/Upload";
        //下载地址
        DownloadPath = "/Download";
        //HttpPage地址
        HttpPagePath = "/HtmlPage";
        List<String> defaultService = new ArrayList<>();
        defaultService.add(WebSocketPath.replace('/', '.'));
        defaultService.add(UploadPath.replace('/', '.'));
        defaultService.add(DownloadPath.replace('/', '.'));
        defaultService.add(HttpPagePath.replace('/', '.'));
        DefaultService = defaultService;
    }

    /**
     * 参数/字段组
     */

    // Redis-pool Redisson-pool
    public static String RedisHost = properties.getProperty("RedisHost");
    public static int RedisPort = Integer.parseInt(properties.getProperty("RedisPort"));
    public static int RedisTimeOut = Integer.parseInt(properties.getProperty("RedisTimeOut"));
    public static String RedisAuth = properties.getProperty("RedisAuth", "");
    public static int RedisDatabase = Integer.parseInt(properties.getProperty("RedisDatabase"));

    // jdbc-mysql && MariaDB
    public static String JdbcDriver = "com.mysql.cj.jdbc.Driver";
    public static String JdbcHost = properties.getProperty("JdbcHost");
    public static String JdbcPort = properties.getProperty("JdbcPort");
    public static String JdbcUser = properties.getProperty("JdbcUser");
    public static String JdbcDBName = properties.getProperty("JdbcDBName");
    public static String JdbcPassword = properties.getProperty("JdbcPassword");

    // mongoDB
    public static String MongoHost = properties.getProperty("MongoHost");
    public static String MongoPort = properties.getProperty("MongoPort");
    public static String MongoDBName = properties.getProperty("MongoDBName");

    //服务器-静态资源路径
    public static String ResourcePath = "/Users/ayl/workspace/resource/";
    //服务器-上传时临时文件路径
    public static String UploadFilePath = "/Users/ayl/workspace/upload/";
    //服务器-下载文件路径
    public static String DownloadFilePath = "/Users/ayl/workspace/download/";
    //SocketPort
    public static int SocketPort = 8888;
    //http请求聚合字节最大长度
    public static int MaxContentLength = 1024 * 1048576;
    //WebSocket读空闲时间闲置/秒
    public static int ReaderIdleTimeSeconds = 80;
    //WebSocket写空闲时间闲置/秒
    public static int WriterIdleTimeSeconds = 100;
    //WebSocket所有空闲时间闲置/秒
    public static int AllIdleTimeSeconds = 120;
    //BACKLOG值用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
    public static int ChannelOptionSoBacklogValue = 1024;
    //处理下载请求响应文件流块大小
    public static int ChunkSize = 8 * 1024;

    /**
     * 枚举组
     */

    //上下文类型
    public enum ContentType {
        none, error, text, html, json, css, js, jpeg, jpg, png, gif, ico, mp3, mp4, data
    }

    //文件类型
    public enum FileType {
        other, txt, html, json, css, js, jpeg, jpg, png, gif, ico, mp3, mp4, mkv, data, folder
    }

    //请求类型
    public enum Command {
        get, post
    }

    //对象类型
    public enum ClassType {
        void_, string_, integer_, long_, double_, float_, boolean_,
        strings_, integers_, longs_, doubles_, floats_, booleans_, enum_,
        json_, jsons_, class_,
    }

    //返回结果类型
    public enum RequestType {
        none, service, htmlPage, resource, upload, download, redirect, admin, websocket, stream, http
    }

    /**
     * Method组
     */

    public static JsonObject Json_Find_Exception = JsonObject.Fail("出现异常.");
    public static JsonObject Json_No_Impl = JsonObject.Fail("该服务不存在具体实现.");
    public static JsonObject Json_Error_Param = JsonObject.Fail("接口传参不正确.");
    public static JsonObject Json_No_InterFace = JsonObject.Fail("不存在该接口.");
    public static JsonObject Json_No_Service = JsonObject.Fail("不存在该服务.");
    public static JsonObject Json_Not_Mobile = JsonObject.Fail("账号不是手机号.");
    public static JsonObject Json_Not_Password = JsonObject.Fail("密码错误.");
    public static JsonObject Json_No_User = JsonObject.Fail("用户不存在.");
    public static JsonObject Json_Not_keyword = JsonObject.Fail("关键词不正确.");

}
