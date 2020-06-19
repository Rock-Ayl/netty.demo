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

/**
 * created by Rock-Ayl on 2019-11-21
 * 上传请求处理器
 */
public class UploadFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileHandler.class);

    //解析收到的文件
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    //文件实体
    private FileEntry fileEntry;
    //文件读写通道
    private FileChannel fileChannel;
    //文件在内存的缓冲区
    protected ByteBuffer fileBuffer;
    //post请求的解码类,它负责把字节解码成Http请求
    private HttpPostRequestDecoder decoder;
    //记录formData
    private HttpData formData;
    //记录form-data中的非文件数据(key value)
    private JsonObject formDataTextJson = JsonObject.VOID();

    static {
        //设置结束时删除临时文件
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        //置空系统临时目录
        DiskFileUpload.baseDirectory = null;
    }

    /**
     * 处理上传请求基本逻辑,验证请求是否合法
     *
     * @param ctx
     * @param msg
     */
    public void filterUpload(ChannelHandlerContext ctx, HttpObject msg) {
        //如果是http请求
        if (msg instanceof HttpRequest) {
            //转化为http请求
            HttpRequest request = (HttpRequest) msg;
            //如果是get请求，返回
            if (HttpMethod.GET == request.method()) {
                //发送错误消息
                ResponseAndEncoderHandler.sendFailAndMessage(ctx, HttpResponseStatus.OK, "upload must use post.");
                //返回
                return;
            }
            //post请求解码
            try {
                //解码请求
                this.decoder = new HttpPostRequestDecoder(this.factory, request);
                //禁用应丢弃缓冲区中读取字节的字节数
                this.decoder.setDiscardThreshold(0);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                //返回错误消息
                ResponseAndEncoderHandler.sendFailAndMessage(ctx, HttpResponseStatus.OK, "upload decoder fail.");
                //返回
                return;
            }
            //是否为Multipart请求
            if (this.decoder.isMultipart() == false) {
                //返回错误消息
                ResponseAndEncoderHandler.sendFailAndMessage(ctx, HttpResponseStatus.OK, "upload must is Multipart");
                //返回
                return;
            }
        }
    }

    /**
     * 处理upload时的文件分块
     *
     * @param ctx
     * @param chunk
     */
    public void handleHttpFormDataContent(ChannelHandlerContext ctx, HttpContent chunk) {
        try {
            this.decoder.offer(chunk);
        } catch (Exception e1) {
            logger.error("handleHttpContent error:", e1);
            //返回错误消息
            ResponseAndEncoderHandler.sendFailAndMessage(ctx, HttpResponseStatus.OK, "文件上传出现错误.");
            //关闭链接
            ctx.channel().close();
            //返回
            return;
        }
        //读取内容并处理
        readHttpFormDataChunkByChunk();
        //最后一个内容
        if (chunk instanceof LastHttpContent) {
            //对该文件进行业务处理并获得返回值
            JsonObject result = FileHandler.instance.uploadFile(this.fileEntry, this.formDataTextJson);
            //响应并关闭
            if (result != null) {
                //响应
                ResponseAndEncoderHandler.sendObject(ctx, HttpResponseStatus.OK, result);
            } else {
                ResponseAndEncoderHandler.sendFailAndMessage(ctx, HttpResponseStatus.OK, "上传失败,业务处理文件响应为空.");
            }
            logger.info("upload FileName=[{}] success.", this.fileEntry.getFileName());
            //重置解码
            resetDecoder();
            //关闭
            ctx.channel().close();
            return;
        }
    }

    /**
     * 根据文件分块读取请求数据
     */
    private void readHttpFormDataChunkByChunk() {
        try {
            //如果存在form-data内容
            while (this.decoder.hasNext()) {
                //获取当前内容
                InterfaceHttpData data = this.decoder.next();
                //判空
                if (data != null) {
                    try {
                        //解析内容并记录
                        parsingFormData(data);
                    } catch (Exception e) {
                        logger.error("解析form-data错误:[{}]", e);
                    } finally {
                        //释放
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            logger.error("readHttpFormDataChunkByChunk", e1);
        }
    }

    /**
     * 解析form-data的参数和文件
     *
     * @param data
     * @throws IOException
     */
    private void parsingFormData(InterfaceHttpData data) throws IOException {
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
                this.formDataTextJson.put(key, value);
                //跳过
                break;
            //文件
            case FileUpload:
                FileUpload fileUpload = (FileUpload) data;
                //如果数据已经存储完毕
                if (fileUpload.isCompleted()) {
                    //创建文件实体
                    this.fileEntry = new FileEntry();
                    //文件fileId
                    this.fileEntry.setFileId(IdUtils.newId());
                    //文件名
                    this.fileEntry.setFileName(getFileNameFromFileUpload(fileUpload));
                    //文件大小
                    this.fileEntry.setFileSize(fileUpload.length());
                    //文件后缀
                    this.fileEntry.setFileExt(FilenameUtils.getExtension(this.fileEntry.getFileName()));
                    //文件地址
                    this.fileEntry.setFilePath(Const.UploadFilePath + this.fileEntry.getFileId() + "." + this.fileEntry.getFileExt());
                    //指定文件本身对象,模式rw为：以读取、写入方式打开指定文件。如果该文件不存在，则尝试创建文件
                    this.fileChannel = new RandomAccessFile(fileEntry.getFilePath(), "rw").getChannel();
                    //文件流读写通道(内存)
                    this.fileBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, this.fileEntry.getFileSize());
                    //写入文件
                    this.fileBuffer.put(fileUpload.get());
                    //关闭读写通道
                    this.fileChannel.close();
                    //清除文件缓冲
                    this.fileBuffer.clear();
                }
                break;
        }
    }

    /**
     * 从 FileUpload对象 获取文件名
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

    /**
     * 清除解码
     */
    public void clearDecoder() {
        if (this.decoder != null) {
            this.decoder.cleanFiles();
        }
    }

    /**
     * 重置解码
     */
    private void resetDecoder() {
        this.decoder.destroy();
        this.decoder = null;
    }

}
