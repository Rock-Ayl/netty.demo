package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.common.entry.FileEntry;
import cn.ayl.util.Base64Utils;
import cn.ayl.util.StringUtils;
import cn.ayl.common.json.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.FilenameUtils;
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
    //上传文件时请求header中不需要组装到fileObject对象的信息
    private static final HashSet<String> headerFilters = new HashSet();
    private boolean readingChunks;
    private HttpData partialContent;
    private HttpPostRequestDecoder decoder;
    private Map<String, String> attrs = new ConcurrentHashMap<>();
    //是否为 multipart 请求
    protected boolean isMultipart = true;
    private int fileBufferSize = 0;
    private FileChannel fileChannel;
    protected ByteBuffer fileBuffer;
    //文件实体
    private FileEntry file;

    static {
        //设置结束时删除临时文件
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        //置空系统临时目录
        DiskFileUpload.baseDirectory = null;
        //初始化headerFilters
        initHeaderFilters();
    }

    //初始化headerFilters
    private static void initHeaderFilters() {
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

    //todo 上传业务逻辑,根据fileEntry去处理
    public void handleUpload() {

    }

    public void handleRequest(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //如果是http请求
        if (msg instanceof HttpRequest) {
            //创建文件实体
            file = new FileEntry();
            //创建文件其他参数对象
            JsonObject fileObject = JsonObject.VOID();
            //转化为http请求
            HttpRequest request = (HttpRequest) msg;
            //如果是get请求，返回
            if (request.method().equals(HttpMethod.GET)) {
                ResponseHandler.sendMessageForJson(ctx, HttpResponseStatus.OK, "upload must use post.");
                return;
            }
            //Post解码
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
                //禁用丢弃字节
                decoder.setDiscardThreshold(0);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                //返回错误
                ResponseHandler.sendMessageForJson(ctx, HttpResponseStatus.OK, e1.getMessage());
                return;
            }
            //请求头
            HttpHeaders headers = request.headers();
            //是否为Multipart请求
            isMultipart = decoder.isMultipart();
            //文件大小
            String v = headers.get(Const.FileSize, "0");
            if (v.equals("0")) {
                v = headers.get("content-length");
            }
            file.setFileSize(Integer.parseInt(v));
            //文件名
            String fileName = headers.get(Const.FileName, "");
            //如果是中文，用base64解码一下
            if (fileName.indexOf(".") <= 0) {
                //解码
                fileName = Base64Utils.decode64(fileName);
            }
            file.setFileName(fileName);
            //文件后缀
            file.setFileExt(FilenameUtils.getExtension(fileName));
            //生成一个文件唯一id
            file.setFileId(StringUtils.newId());
            //文件创建时间
            file.setFileCreateTime(headers.get(Const.FileCreateTime, "0"));
            //文件修改时间
            file.setFileUpdateTime(headers.get(Const.FileUpdateTime, "0"));
            //文件地址
            file.setFilePath(Const.UploadFilePath + file.getFileId() + "." + file.getFileExt());
            //指定文件本身对象，模式rw为：以读取、写入方式打开指定文件。如果该文件不存在，则尝试创建文件
            fileChannel = new RandomAccessFile(file.getFilePath(), "rw").getChannel();
            //文件流(内存)，写入文件长度
            fileBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, file.getFileSize());
            //文件创建时间
            fileObject.append(Const.FileCreateTime, Long.parseLong(headers.get(Const.FileCreateTime, "0")));
            //将文件的附属信息全部存入文件其他信息对象中
            for (Iterator<Map.Entry<String, String>> i = request.headers().iteratorAsString(); i.hasNext(); ) {
                Map.Entry<String, String> entry = i.next();
                //该headers的key
                String key = entry.getKey();
                //过滤一些不需要添加的参数
                if (key.startsWith("File") || headerFilters.contains(key)) {
                    continue;
                }
                //组装
                fileObject.append(key, entry.getValue());
            }
            //组装文件对象参数
            file.setFileObject(fileObject);
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
            if (fileBufferSize == file.getFileSize()) {
                logger.info("upload File[{}] Success", file.getFileName());
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
            ResponseHandler.sendMessageForJson(ctx, HttpResponseStatus.OK, e1.getMessage());
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
            ResponseHandler.sendMessageForJson(ctx, HttpResponseStatus.OK, "OK");
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
                    file.setFileName(fileUpload.getFilename());
                    if (org.apache.commons.lang3.StringUtils.isEmpty(file.getFileName())) {
                        file.setFileName(fileUpload.getName());
                    }
                    fileBuffer.put(fileUpload.getByteBuf().array());
                    fileChannel.close();
                    fileBuffer.clear();
                    doUploadService();
                    //响应并关闭
                    ResponseHandler.sendForJson(ctx, HttpResponseStatus.OK, file.toJson());
                    logger.info("upload FileName=[{}] success.", file.getFileName());
                }
            }
        }
    }

    private void doUploadService() {
        handleUpload();
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
