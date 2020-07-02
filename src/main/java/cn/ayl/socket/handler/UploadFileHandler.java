package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.common.entry.FileEntry;
import cn.ayl.handler.FileHandler;
import cn.ayl.socket.encoder.ResponseAndEncoderHandler;
import cn.ayl.socket.rpc.Context;
import cn.ayl.util.IdUtils;
import cn.ayl.common.json.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * created by Rock-Ayl on 2019-11-21
 * 上传请求处理器
 */
public class UploadFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileHandler.class);

    //解析收到的文件方式
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    //post请求的解码类,它负责把字节解码成Http请求
    private HttpPostRequestDecoder decoder;

    //请求上下文
    private Context context;
    //文件实体列表
    public List<FileEntry> fileEntryList = new ArrayList<>();
    //记录所有进来的参数,包括form-data、cookieId
    private JsonObject params = JsonObject.VOID();

    static {
        //设置结束时删除临时文件
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        //置空系统临时目录
        DiskFileUpload.baseDirectory = null;
    }

    public UploadFileHandler(Context context) {
        this.context = context;
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
            //存放cookieId、当前登录用户id到参数中
            this.params.append(Const.CookieId, this.context.user.cookieId);
            this.params.append(Const.CtxUserId, this.context.user.cookieId);
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
        //读取form-data内容并记录
        readHttpFormData();
        //最后一个内容
        if (chunk instanceof LastHttpContent) {
            //对该文件进行业务处理并获得返回值
            JsonObject result = FileHandler.instance.uploadFile(this.fileEntryList, this.params);
            //响应并关闭
            if (result != null) {
                //响应
                ResponseAndEncoderHandler.sendObject(ctx, HttpResponseStatus.OK, result);
            } else {
                ResponseAndEncoderHandler.sendFailAndMessage(ctx, HttpResponseStatus.OK, "上传失败,业务处理文件响应为空.");
            }
            //判空
            if (CollectionUtils.isNotEmpty(this.fileEntryList)) {
                //循环
                for (FileEntry fileEntry : this.fileEntryList) {
                    //打印
                    logger.info("upload FileName=[{}] success.", fileEntry.getFileName());
                }
            }
            //重置解码
            resetDecoder();
            //关闭
            ctx.channel().close();
            //返回
            return;
        }
    }

    /**
     * 根据文件分块读取请求数据
     */
    private void readHttpFormData() {
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
            logger.error("readHttpFormData", e1);
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
                this.params.append(key, value);
                //跳过
                break;
            //文件
            case FileUpload:
                FileUpload fileUpload = (FileUpload) data;
                //如果数据已经存储完毕
                if (fileUpload.isCompleted()) {
                    //创建文件实体
                    FileEntry fileEntry = new FileEntry();
                    //文件fileId
                    fileEntry.setFileId(IdUtils.newId());
                    //文件名
                    fileEntry.setFileName(fileUpload.getFilename());
                    //文件大小
                    fileEntry.setFileSize(fileUpload.length());
                    //如果文件大小为0或者文件名为null
                    if (fileEntry.getFileSize() == 0 || StringUtils.isBlank(fileEntry.getFileName())) {
                        //丢弃空文件
                        return;
                    }
                    //文件后缀
                    fileEntry.setFileExt(FilenameUtils.getExtension(fileEntry.getFileName()));
                    //如果文件存在后缀名
                    if (StringUtils.isNotBlank(fileEntry.getFileExt())) {
                        //有后缀的文件地址
                        fileEntry.setFilePath(Const.UploadFilePath + fileEntry.getFileId() + "." + fileEntry.getFileExt());
                    } else {
                        //无后缀的文件地址
                        fileEntry.setFilePath(Const.UploadFilePath + fileEntry.getFileId());
                    }
                    //存储进List
                    this.fileEntryList.add(fileEntry);
                    //指定文件本身对象,模式rw为：以读取、写入方式打开指定文件。如果该文件不存在，则尝试创建文件
                    FileChannel fileChannel = new RandomAccessFile(fileEntry.getFilePath(), "rw").getChannel();
                    //获取文件的内存缓冲:读写、起始字节位置、文件大小
                    ByteBuffer fileBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileEntry.getFileSize());
                    //写入byte[]
                    fileBuffer.put(fileUpload.get());
                    //关闭这个通道
                    fileChannel.close();
                    //初始化文件缓冲位置
                    fileBuffer.clear();
                }
                break;
        }
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
