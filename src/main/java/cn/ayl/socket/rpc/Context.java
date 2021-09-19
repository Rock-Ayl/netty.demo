package cn.ayl.socket.rpc;

import cn.ayl.common.enumeration.RequestType;
import cn.ayl.pojo.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

/**
 * created by Rock-Ayl 2019-12-10
 * 一个Context对应一个物理的数据链路(就是一个当前的会话)
 */
public class Context {

    //请求类型
    private RequestType requestType = RequestType.none;
    //请求者ip
    private String ip;
    //UriPath
    private String uriPath;
    //ChannelId
    private ChannelId channelId;
    //Channel
    private Channel channel;
    //用户信息
    private User user = new User();


    //受保护的,不允许new,但允许继承
    protected Context() {

    }

    /**
     * 初始化context
     *
     * @param type
     * @param channel
     * @return
     */
    public static Context initContext(RequestType type, Channel channel) {
        //初始化
        Context context = new Context();
        //组装参数
        context.setRequestType(type);
        context.setChannelId(channel.id());
        context.setChannel(channel);
        context.setIp(channel.remoteAddress().toString());
        //返回
        return context;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
