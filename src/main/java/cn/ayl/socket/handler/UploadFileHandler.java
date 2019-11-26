package cn.ayl.socket.handler;

import cn.ayl.util.Base64Convert;
import cn.ayl.util.StringUtil;
import cn.ayl.util.json.JsonObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * created by Rock-Ayl on 2019-11-21
 * 上传请求处理器
 */
public class UploadFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileHandler.class);

    private final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static final HashSet<String> headerFilters = new HashSet();
    private boolean readingChunks;
    private HttpData partialContent;
    private HttpPostRequestDecoder decoder;
    private Map<String, String> attrs = new ConcurrentHashMap<>();
    //是否为 multipart 请求
    protected boolean isMultipart = true;
    //文件大小
    private int fileSize = 0;
    private int fileBufferSize = 0;
    private String fileName;
    //文件路径
    private String filePath;
    //文件后缀
    private String fileExt;
    private FileChannel fileChannel;
    private String fileId;
    protected ByteBuffer fileBuffer;
    //用来存放处理业务的文件属性
    private JsonObject fileObject;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = null;
        headerFilters.add("Referer");
        headerFilters.add("Host");
        headerFilters.add("Connection");
        headerFilters.add("Accept");
        headerFilters.add("Origin");
        headerFilters.add("byCookie-Agent");
        headerFilters.add("Content-Type");
        headerFilters.add("Content-Length");
        headerFilters.add("Accept-Encoding");
        headerFilters.add("Accept-Language");
        headerFilters.add("Cache-Control");
        headerFilters.add("Postman-Token");
        headerFilters.add("Cookie");
    }

    //todo 上传业务逻辑
    public void handleUpload(String fileId, String fileExt, JsonObject fileObject) {

    }

    public void handleRequest(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            fileObject = new JsonObject();
            HttpRequest request = (HttpRequest) msg;
            if (request.method().equals(HttpMethod.GET)) {
                ResponseHandler.sendMessage(ctx, HttpResponseStatus.OK, "upload must use post.");
                return;
            }
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
                decoder.setDiscardThreshold(0);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                ResponseHandler.sendMessage(ctx, HttpResponseStatus.OK, e1.getMessage());
                return;
            }
            //请求头
            HttpHeaders headers = request.headers();
            //是否为Multipart请求
            isMultipart = decoder.isMultipart();
            //文件大小
            String v = headers.get("FileSize", "0");
            if (v.equals("0")) {
                v = headers.get("content-length");
            }
            fileSize = Integer.parseInt(v);
            fileObject.append("fileSize", fileSize);
            //文件名
            fileName = headers.get("FileName", "");
            //如果是中文，用base64解码一下
            if (fileName.indexOf(".") <= 0) {
                //解码
                fileName = Base64Convert.decode64(fileName);
            }
            fileObject.append("fileName", fileName);
            //文件后缀
            fileExt = FilenameUtils.getExtension(fileName);
            fileObject.append("fileExt", fileExt);
            //生成一个文件唯一id
            fileId = StringUtil.newId();
            fileObject.append("fileId", fileId);
            //文件地址
            filePath = "/workspace" + "/" + fileId + "." + fileExt;
            fileObject.append("filePath", filePath);
            //文件通道，创建一个空的0K文件
            fileChannel = new RandomAccessFile(filePath, "rw").getChannel();
            //文件流(内存)，写入文件大小，没有文件内容
            fileBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            //文件创建时间
            fileObject.append("fileDate", Long.parseLong(headers.get("FileDate", "0")));
            for (Iterator<Map.Entry<String, String>> i = request.headers().iteratorAsString(); i.hasNext(); ) {
                Map.Entry<String, String> entry = i.next();
                String key = entry.getKey();
                if (key.startsWith("File") || headerFilters.contains(key)) continue;
                fileObject.append(key, entry.getValue());
            }
            readingChunks = HttpUtil.isTransferEncodingChunked(request);
        }
    }

    /**
     * 处理upload时的文件分块
     *
     * @param ctx
     * @param chunk
     * @throws Exception
     */
    public void handleHttpContent(ChannelHandlerContext ctx, HttpContent chunk) throws Exception {
        //
        if (this.isMultipart == false) {
            ByteBuffer buffer = chunk.content().nioBuffer();
            while (buffer.hasRemaining()) {
                fileBufferSize += buffer.remaining();
                fileBuffer.put(buffer);
            }
            if (fileBufferSize == fileSize) {
                logger.info("upload File[{}] Success", fileName);
                fileChannel.close();
                fileBuffer.clear();
                doUploadService();
            }
            return;
        }
        try {
            decoder.offer(chunk);
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
            //返回错误消息
            ResponseHandler.sendMessage(ctx, HttpResponseStatus.OK, e1.getMessage());
            ctx.channel().close();
            return;
        }
        //根据文件分块读取请求数据
        readHttpDataChunkByChunk(ctx);
        //最后一个分快
        if (chunk instanceof LastHttpContent) {
            readingChunks = false;
            reset();
            //发送消息
            ResponseHandler.sendMessage(ctx, HttpResponseStatus.OK, "OK");
            //关闭
            ctx.channel().close();
            return;
        }
    }

    /**
     * 根据文件分块读取请求数据
     *
     * @param ctx
     */
    private void readHttpDataChunkByChunk(ChannelHandlerContext ctx) {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    if (partialContent == data) {
                        partialContent = null;
                    }
                    try {
                        //写数据
                        writeHttpData(ctx, data);
                    } catch (Exception e) {
                    } finally {
                        data.release();
                    }
                }
            }
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            logger.error("readHttpDataChunkByChunk", e1);
        }
    }

    /**
     * 写入文件
     *
     * @param ctx
     * @param data
     * @throws IOException
     */
    private void writeHttpData(ChannelHandlerContext ctx, InterfaceHttpData data) throws IOException {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getString(CharsetUtil.UTF_8);
                attrs.put(attribute.getName(), value);
            } catch (IOException e1) {
                return;
            }
        } else {
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
                    doUploadService();
                    ctx.writeAndFlush(ResponseHandler.getResponseOKAndJson(HttpResponseStatus.OK, fileObject)).addListener(ChannelFutureListener.CLOSE);
                    logger.info("upload FileName=[{}] success.", fileName);
                }
            }
        }
    }

    private void doUploadService() {
        handleUpload(fileId, fileExt, fileObject);
    }

    public void clear() {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    private void reset() {
        decoder.destroy();
        decoder = null;
    }

}
