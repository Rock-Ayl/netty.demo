package cn.ayl.socket.handler;

import cn.ayl.util.json.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created By Rock-Ayl on 2019-11-15
 * 当Http请求为上传文件时,交给该处理器处理
 */
public class UploadFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileHandler.class);
    private final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private HttpData partialContent;
    private HttpPostRequestDecoder decoder;
    private String filePath = "C:\\Users\\Rocy-Ayl\\Desktop\\";
    private int fileSize;
    private String fileExt;
    private String fileName;
    private FileChannel fileChannel;
    protected ByteBuffer fileBuffer;

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
        FullHttpResponse response = null;
        //todo 处理上传请求
        FullHttpRequest request = req;
        if (request.method().equals(HttpMethod.GET)) {
            response = ResponseHandler.responseOKAndJson(HttpResponseStatus.OK, "upload must use post.");
        }
        try {
            decoder = new HttpPostRequestDecoder(factory, request);
            decoder.setDiscardThreshold(0);
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
            logger.error("错误:{}", e);
        }

        HttpHeaders headers = request.headers();
        fileSize = headers.getInt("fileSize");
        fileName = headers.get("fileName");
        fileExt = headers.get("fileExt");
        filePath = filePath + fileName + "." + fileExt;
        InterfaceHttpData data = null;
        try {
            fileChannel = new RandomAccessFile(filePath, "rw").getChannel();
            fileBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            data = decoder.next();
            if (data != null) {
                if (partialContent == data) {
                    partialContent = null;
                }
            }
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    fileName = fileUpload.getFilename();
                    if (StringUtils.isEmpty(fileName)) {
                        fileName = fileUpload.getName();
                    }
                    fileBuffer.put(fileUpload.getByteBuf().array());
                    fileChannel.close();
                    fileBuffer.clear();
                    logger.info("upload FileName=[{}] success.", fileName);
                }
            }

            response = ResponseHandler.responseOKAndJson(HttpResponseStatus.OK, JsonObject.Success());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            data.release();
        }
        // 发送响应并关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
