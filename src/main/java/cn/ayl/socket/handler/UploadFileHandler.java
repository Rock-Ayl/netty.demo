package cn.ayl.socket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created By Rock-Ayl on 2019-11-15
 * 当Http请求为上传文件时,交给该处理器处理
 */
public class UploadFileHandler {

    //通道处理器
    ChannelHandlerContext ctx;
    //请求
    FullHttpRequest req;
    //请求路径
    String path;

    public UploadFileHandler(ChannelHandlerContext ctx, FullHttpRequest req, String path) {
        this.ctx = ctx;
        this.req = req;
        this.path = path;
    }

    //处理上传请求
    public void handleRequest() {
        //todo 处理上传请求
    }

}
