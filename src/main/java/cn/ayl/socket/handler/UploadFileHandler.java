package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.common.entry.FileEntry;
import cn.ayl.handler.FileHandler;
import cn.ayl.socket.encoder.ResponseAndEncoderHandler;
import cn.ayl.util.IdUtils;
import cn.ayl.common.json.JsonObject;
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
    //上传文件时请求header中不需要组装到fileObject对象的信息
    private static final HashSet<String> headerFilters = new HashSet();
    private boolean readingChunks;
    private HttpData partialContent;
    private HttpPostRequestDecoder decoder;
    //存储 form-data中的非文件数据(key value)
    private Map<String, String> formDataTextMap = new ConcurrentHashMap<>();
    //是否为 multipart 请求
    protected boolean isMultipart = true;
    private int fileBufferSize = 0;
    private FileChannel fileChannel;
    protected ByteBuffer fileBuffer;
    //文件实体
    private FileEntry file;
    //业务返回结果
    private JsonObject result;

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

    /**
     * 处理上传业务
     *
     * @return
     */
    private void uploadService(FileEntry fileEntry) {
        this.result = FileHandler.instance.uploadFile(fileEntry);
    }

    public void handleRequest(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //如果是http请求
        if (msg instanceof HttpRequest) {
            //创建文件实体
            this.file = new FileEntry();
            //创建文件其他参数对象
            JsonObject fileObject = JsonObject.VOID();
            //转化为http请求
            HttpRequest request = (HttpRequest) msg;
            //如果是get请求，返回
            if (HttpMethod.GET == request.method()) {
                //发送错误消息
                ResponseAndEncoderHandler.sendMessageOfJson(ctx, HttpResponseStatus.OK, "upload must use post.");
                //返回
                return;
            }
            //Post解码
            try {
                this.decoder = new HttpPostRequestDecoder(this.factory, request);
                //禁用丢弃字节
                this.decoder.setDiscardThreshold(0);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                //返回错误消息
                ResponseAndEncoderHandler.sendMessageOfJson(ctx, HttpResponseStatus.OK, e1.getMessage());
                //返回
                return;
            }
            //请求头
            HttpHeaders headers = request.headers();
            //是否为Multipart请求
            this.isMultipart = this.decoder.isMultipart();
            //文件大小
            String v = headers.get(Const.FileSize, "0");
            if (v.equals("0")) {
                v = headers.get("content-length");
            }
            this.file.setFileSize(Integer.parseInt(v));
            //文件后缀
            this.file.setFileExt(FilenameUtils.getExtension(this.file.getFileName()));
            //生成一个文件唯一id
            this.file.setFileId(IdUtils.newId());
            //文件创建时间
            this.file.setFileCreateTime(headers.get(Const.FileCreateTime, "0"));
            //文件修改时间
            this.file.setFileUpdateTime(headers.get(Const.FileUpdateTime, "0"));
            //文件地址
            this.file.setFilePath(Const.UploadFilePath + this.file.getFileId() + "." + this.file.getFileExt());
            //指定文件本身对象，模式rw为：以读取、写入方式打开指定文件。如果该文件不存在，则尝试创建文件
            this.fileChannel = new RandomAccessFile(file.getFilePath(), "rw").getChannel();
            //文件流(内存)，写入文件长度
            this.fileBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, this.file.getFileSize());
            //文件创建时间
            fileObject.append(Const.FileCreateTime, Long.parseLong(headers.get(Const.FileCreateTime, "0")));
            //将文件的附属信息全部存入文件其他信息对象中
            for (Iterator<Map.Entry<String, String>> i = request.headers().iteratorAsString(); i.hasNext(); ) {
                Map.Entry<String, String> entry = i.next();
                //该headers的key
                String key = entry.getKey();
                //过滤一些不需要添加的参数
                if (key.startsWith("File") || this.headerFilters.contains(key)) {
                    continue;
                }
                //组装
                fileObject.append(key, entry.getValue());
            }
            //组装文件对象参数
            this.file.setFileObject(fileObject);
            this.readingChunks = HttpUtil.isTransferEncodingChunked(request);
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
        if (this.isMultipart == false) {
            ByteBuffer buffer = chunk.content().nioBuffer();
            while (buffer.hasRemaining()) {
                this.fileBufferSize += buffer.remaining();
                this.fileBuffer.put(buffer);
            }
            if (this.fileBufferSize == this.file.getFileSize()) {
                logger.info("upload File[{}] Success", this.file.getFileName());
                this.fileChannel.close();
                this.fileBuffer.clear();
                //处理上传业务
                uploadService(this.file);
            }
            return;
        }
        try {
            this.decoder.offer(chunk);
        } catch (Exception e1) {
            logger.error("handleHttpContent error:", e1);
            //返回错误消息
            ResponseAndEncoderHandler.sendMessageOfJson(ctx, HttpResponseStatus.OK, "文件上传出现错误.");
            //关闭链接
            ctx.channel().close();
            //返回
            return;
        }
        //根据文件分块读取请求数据
        readHttpDataChunkByChunk(ctx);
        //最后一个分快
        if (chunk instanceof LastHttpContent) {
            this.readingChunks = false;
            reset();
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
            while (this.decoder.hasNext()) {
                InterfaceHttpData data = this.decoder.next();
                if (data != null) {
                    if (this.partialContent == data) {
                        this.partialContent = null;
                    }
                    try {
                        //写数据
                        parsingFormData(ctx, data);
                    } catch (Exception e) {
                    } finally {
                        data.release();
                    }
                }
            }
            InterfaceHttpData data = this.decoder.currentPartialHttpData();
            if (data != null) {
                if (this.partialContent == null) {
                    this.partialContent = (HttpData) data;
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            logger.error("readHttpDataChunkByChunk", e1);
        }
    }

    /**
     * 解析form-data的参数和文件
     *
     * @param ctx
     * @param data
     * @throws IOException
     */
    private void parsingFormData(ChannelHandlerContext ctx, InterfaceHttpData data) throws IOException {
        //判断该数据类型
        switch (data.getHttpDataType()) {
            //text
            case Attribute:
                //获取text对象
                Attribute attribute = (Attribute) data;
                //获取form中的key
                String key = attribute.getName();
                //获取form中的value
                String value = attribute.getString(CharsetUtil.UTF_8);
                //组装
                this.formDataTextMap.put(key, value);
                //跳过
                break;
            //文件
            case FileUpload:
                FileUpload fileUpload = (FileUpload) data;
                //如果数据已经存储完毕
                if (fileUpload.isCompleted()) {
                    //获取文件名
                    this.file.setFileName(getFileNameFromFileUpload(fileUpload));
                    this.fileBuffer.put(fileUpload.get());
                    this.fileChannel.close();
                    this.fileBuffer.clear();
                    //处理上传业务
                    uploadService(this.file);
                    //响应并关闭
                    if (this.result != null) {
                        //响应
                        ResponseAndEncoderHandler.sendObject(ctx, HttpResponseStatus.OK, this.result);
                    } else {
                        ResponseAndEncoderHandler.sendObject(ctx, HttpResponseStatus.OK, this.file.toJson());
                    }
                    logger.info("upload FileName=[{}] success.", this.file.getFileName());
                }
                break;
        }
    }

    /**
     * 从 FileUpload 对象中获取文件名
     *
     * @param fileUpload
     * @return
     */
    private String getFileNameFromFileUpload(FileUpload fileUpload) {
        //获取文件名
        String fileName = fileUpload.getFilename();
        //判空
        if (StringUtils.isEmpty(fileName)) {
            //用默认
            fileName = fileUpload.getName();
        }
        //返回
        return fileName;
    }

    public void clear() {
        if (this.decoder != null) {
            this.decoder.cleanFiles();
        }
    }

    private void reset() {
        this.decoder.destroy();
        this.decoder = null;
    }

}
