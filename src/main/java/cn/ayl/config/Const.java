package cn.ayl.config;

import cn.ayl.socket.rpc.Context;
import cn.ayl.util.PropertyUtils;
import cn.ayl.common.json.JsonObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;

/**
 * created by Rock-Ayl 2019-11-5
 * 常量
 */
public class Const {

    /**
     * Method组
     */

    public final static JsonObject Json_Find_Exception = JsonObject.Fail("出现异常.");
    public final static JsonObject Json_No_Impl = JsonObject.Fail("该服务不存在具体实现.");
    public final static JsonObject Json_Error_Param = JsonObject.Fail("接口传参不正确.");
    public final static JsonObject Json_No_InterFace = JsonObject.Fail("不存在该接口.");
    public final static JsonObject Json_No_Service = JsonObject.Fail("不存在该服务.");
    public final static JsonObject Json_Not_Mobile = JsonObject.Fail("账号不是手机号.");
    public final static JsonObject Json_Not_Password = JsonObject.Fail("密码错误.");
    public final static JsonObject Json_No_User = JsonObject.Fail("用户不存在.");
    public final static JsonObject Json_Not_Keyword = JsonObject.Fail("关键词不正确.");
    public final static JsonObject Json_No_Permission = JsonObject.Fail("没有权限.");

    /**
     * netty channel的上下文 AttributeKey
     */

    public final static AttributeKey<Context> AttrContext = AttributeKey.valueOf("Context");

    /**
     * 配置文件对象
     */

    public final static PropertyUtils properties = new PropertyUtils().use("setting.properties");

    /**
     * 常用字段
     */

    public final static String SPACE = " ";
    public final static String Message = "message";
    public final static String Items = "items";
    public final static String CookieId = "cookieId";
    public final static String Data = "data";
    public final static String IsSuccess = "isSuccess";
    public final static String TotalCount = "totalCount";
    public final static String Type = "type";
    public final static String FileId = "fileId";
    public final static String CtxUserId = "ctxUserId";
    public final static String FileCreateTime = "fileCreateTime";
    public final static String FileUpdateTime = "FileUpdateTime";
    public final static String FileName = "fileName";
    public final static String FileExt = "fileExt";
    public final static String FilePath = "filePath";
    public final static String FileSize = "fileSize";
    public final static String FileObject = "fileObject";

    /**
     * 初始化除了Service外的其他几个服务
     */

    //WebSocket地址
    public static final String WebSocketPath = "/WebSocket";
    //上传地址
    public static final String UploadPath = "/Upload";
    //下载地址
    public static final String DownloadPath = "/Download";
    //默认的服务初始化
    public static List<String> DefaultService;

    static {
        DefaultService = new ArrayList<>();
        DefaultService.add(WebSocketPath.replace('/', '.'));
        DefaultService.add(UploadPath.replace('/', '.'));
        DefaultService.add(DownloadPath.replace('/', '.'));
    }

    /**
     * 参数/字段组
     */

    // Redis-pool Redisson-pool
    public static final String RedisHost = properties.getProperty("RedisHost");
    public static final int RedisPort = Integer.parseInt(properties.getProperty("RedisPort"));
    public static final int RedisTimeOut = Integer.parseInt(properties.getProperty("RedisTimeOut"));
    public static final String RedisAuth = properties.getProperty("RedisAuth", "");
    public static final int RedisDatabase = Integer.parseInt(properties.getProperty("RedisDatabase"));

    // jdbc-mysql && MariaDB
    public static final String JdbcDriver = "com.mysql.cj.jdbc.Driver";
    public static final String JdbcHost = properties.getProperty("JdbcHost");
    public static final String JdbcPort = properties.getProperty("JdbcPort");
    public static final String JdbcUser = properties.getProperty("JdbcUser");
    public static final String JdbcDBName = properties.getProperty("JdbcDBName");
    public static final String JdbcPassword = properties.getProperty("JdbcPassword");

    // mongoDB
    public static final String MongoHost = properties.getProperty("MongoHost");
    public static final String MongoPort = properties.getProperty("MongoPort");
    public static final String MongoDBName = properties.getProperty("MongoDBName");

    //当前支持的http协议版本
    public static final HttpVersion CurrentHttpVersion = HttpVersion.HTTP_1_1;

    //服务器-静态资源路径
    public static final String ResourceFilePath = "/Users/ayl/workspace/resource/";
    //服务器-上传时临时文件路径
    public static final String UploadFilePath = "/Users/ayl/workspace/upload/";
    //服务器-下载文件路径
    public static final String DownloadFilePath = "/Users/ayl/workspace/download/";

    //SocketPort
    public static final int SocketPort = 8888;
    //http请求聚合字节最大长度
    public static final int MaxContentLength = 1024 * 1048576;

    //WebSocket读空闲时间闲置/秒
    public static final int ReaderIdleTimeSeconds = 80;
    //WebSocket写空闲时间闲置/秒
    public static final int WriterIdleTimeSeconds = 100;
    //WebSocket所有空闲时间闲置/秒
    public static final int AllIdleTimeSeconds = 120;

    //BACKLOG值用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
    public static final int ChannelOptionSoBacklogValue = 1024;
    //静态文件过期时间(秒)
    public static final long FileResourceExpiresTime = 60 * 1000;

    //服务名字
    public static final String ServerName = "netty.demo";

}
