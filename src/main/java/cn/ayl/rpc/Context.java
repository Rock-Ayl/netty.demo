package cn.ayl.rpc;

import cn.ayl.config.Const;
import cn.ayl.util.json.JsonObject;
import io.netty.channel.Channel;

/**
 * 一个Context对应一个物理的数据链路(一个当前的会话)
 */
public class Context {

    //请求类型
    public Const.RequestType requestType;
    //请求者ip
    public String ip;
    //请求Channel
    public Channel channel;
    //会话参数
    public JsonObject parameterObject = JsonObject.VOID();

    //创建上下文
    public static Context createContext(Const.RequestType type, Channel channel) {
        Context context = new Context();
        context.requestType = type;
        context.channel = channel;
        context.ip = channel.remoteAddress().toString();
        return context;
    }

}
