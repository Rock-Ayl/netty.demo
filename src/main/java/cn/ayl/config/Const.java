package cn.ayl.config;

import cn.ayl.util.json.JsonObject;

/**
 * created by Rock-Ayl 2019-11-5
 * 常量
 */
public class Const {

    /**
     * 参数/字段组
     */

    //SocketPort
    public static int SocketPort = 8888;
    //http请求聚合字节最大长度
    public static int MaxContentLength = 1024 * 1048576;
    //WebSocket读空闲时间闲置/秒
    public static int ReaderIdleTimeSeconds = 8;
    //WebSocket写空闲时间闲置/秒
    public static int WriterIdleTimeSeconds = 10;
    //WebSocket所有空闲时间闲置/秒
    public static int AllIdleTimeSeconds = 12;
    //BACKLOG值用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
    public static int ChannelOptionSoBacklogValue = 1024;
    public static String WebSocketPath = "/WebSocket";


    /**
     * 枚举组
     */

    //上下文类型
    public enum ContentType {
        none, error, text, html, json, css, js, jpeg, jpg, png, gif, ico, mp3, mp4, data
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
}
