package cn.ayl.config;

import cn.ayl.socket.rpc.Context;
import cn.ayl.util.PropertyUtils;
import cn.ayl.common.json.JsonObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-11-5
 * 常量
 */
public class Const {

    protected static Logger logger = LoggerFactory.getLogger(Const.class);

    /**
     * Netty
     */

    //channel的上下文 AttributeKey
    public final static AttributeKey<Context> AttrContext = AttributeKey.valueOf("Context");

    //服务器-配置路径
    public static final String ConfigPath = System.getProperty("user.dir") + "/conf/";
    //服务器-系统配置文件
    public static PropertyUtils properties;

    //初始化配置文件
    static {
        //配置文件绝对路径
        String settingPath = ConfigPath + "setting.properties";
        try {
            //读取配置文件
            properties = new PropertyUtils().use(settingPath);
        } catch (Exception e) {
            logger.error("读取不到配置文件:[" + settingPath + "],系统关闭.");
            //直接终止
            System.exit(-1);
        }
    }

    //WebSocket读空闲时间闲置/秒
    public static final int ReaderIdleTimeSeconds = 80;
    //WebSocket写空闲时间闲置/秒
    public static final int WriterIdleTimeSeconds = 100;
    //WebSocket所有空闲时间闲置/秒
    public static final int AllIdleTimeSeconds = 120;

    /**
     * Server
     */

    //服务器名
    public static final String ServerName = "netty.demo";
    //服务地址 or Ip
    public static final String SocketAddress = properties.getProperty("SocketAddress");
    //服务端口
    public static final int SocketPort = Integer.parseInt(properties.getProperty("SocketPort"));
    //服务器-静态资源路径
    public static final String ResourceFilePath = properties.getProperty("ResourceFilePath");
    //服务器-上传、下载时临时文件路径
    public static final String UploadFilePath = properties.getProperty("UploadFilePath");

    /**
     * Service
     */

    //WebSocket地址
    public static final String WebSocketPath = "/WebSocket";
    //上传地址
    public static final String UploadPath = "/Upload";
    //下载地址
    public static final String DownloadPath = "/Download";

    /**
     * System Config
     */

    //Redis-pool Redisson-pool
    public static final String RedisHost = properties.getProperty("RedisHost");
    public static final int RedisPort = Integer.parseInt(properties.getProperty("RedisPort"));
    public static final int RedisTimeOut = Integer.parseInt(properties.getProperty("RedisTimeOut"));
    public static final String RedisAuth = properties.getProperty("RedisAuth", "");
    public static final int RedisDatabase = Integer.parseInt(properties.getProperty("RedisDatabase"));

    //Jdbc-Mysql && MariaDB
    public static final String JdbcDriver = "com.mysql.cj.jdbc.Driver";
    public static final String JdbcHost = properties.getProperty("JdbcHost");
    public static final String JdbcPort = properties.getProperty("JdbcPort");
    public static final String JdbcUser = properties.getProperty("JdbcUser");
    public static final String JdbcDBName = properties.getProperty("JdbcDBName");
    public static final String JdbcPassword = properties.getProperty("JdbcPassword");

    //Neo4j
    public static final String Neo4jHost = properties.getProperty("Neo4jHost");
    public static final String Neo4jPort = properties.getProperty("Neo4jPort");
    public static final String Neo4jUser = properties.getProperty("Neo4jUser");
    public static final String Neo4jPassword = properties.getProperty("Neo4jPassword");

    //Etcd
    public static final String EtcdHost = properties.getProperty("EtcdHost");
    public static final String EtcdPort = properties.getProperty("EtcdPort");

    //MongoDB
    public static final String MongoHost = properties.getProperty("MongoHost");
    public static final String MongoPort = properties.getProperty("MongoPort");
    public static final String MongoDBName = properties.getProperty("MongoDBName");

    //ElasticSearch
    public static final String ElasticSearchIp = properties.getProperty("ElasticSearchIp");
    public static final int ElasticSearchPort = Integer.parseInt(properties.getProperty("ElasticSearchPort"));
    public static final String ElasticSearchIndexName = properties.getProperty("ElasticSearchIndexName");

    //当前Http协议
    public static final HttpVersion CurrentHttpVersion = HttpVersion.HTTP_1_1;

    //用户CookieId登过期时间:2个小时
    public static int CookieIdExpiredTime = 3600 * 2;

    /**
     * 常用魔法变量
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
    public final static String FileName = "fileName";
    public final static String FileExt = "fileExt";
    public final static String FilePath = "filePath";
    public final static String FileSize = "fileSize";
    public final static String FileMD5 = "fileMD5";
    public final static String FileUploadTime = "fileUploadTime";

    /**
     * Method组
     */

    public final static JsonObject Json_Find_Exception = JsonObject.Fail("出现异常.");
    public final static JsonObject Json_Parse_Param_Find_Exception = JsonObject.Fail("解析参数出现异常.");
    public final static JsonObject Json_No_Impl = JsonObject.Fail("该服务不存在具体实现.");
    public final static JsonObject Json_Error_Param = JsonObject.Fail("接口传参不正确.");
    public final static JsonObject Json_No_InterFace = JsonObject.Fail("不存在该接口.");
    public final static JsonObject Json_No_Service = JsonObject.Fail("不存在该服务.");
    public final static JsonObject Json_No_ContentType = JsonObject.Fail("不支持该请求类型.");
    public final static JsonObject Json_Not_Mobile = JsonObject.Fail("账号不是手机号.");
    public final static JsonObject Json_Not_Password = JsonObject.Fail("密码错误.");
    public final static JsonObject Json_No_User = JsonObject.Fail("用户不存在.");
    public final static JsonObject Json_Not_Keyword = JsonObject.Fail("关键词不正确.");
    public final static JsonObject Json_No_Permission = JsonObject.Fail("没有权限.");
    public final static JsonObject Json_Query_Fail = JsonObject.Fail("查询失败.");

}
