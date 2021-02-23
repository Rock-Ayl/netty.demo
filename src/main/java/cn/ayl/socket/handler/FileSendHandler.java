package cn.ayl.socket.handler;

import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created By Rock-Ayl on 2021-02-23
 * 文件传输过程监控处理器
 */
public class FileSendHandler implements ChannelProgressiveFutureListener {

    protected static Logger logger = LoggerFactory.getLogger(FileSendHandler.class);

    //传输文件的名称
    private String fileName;

    /**
     * 初始化
     *
     * @param fileName
     * @return
     */
    public static FileSendHandler VOID(String fileName) {
        return new FileSendHandler(fileName);
    }

    private FileSendHandler(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 传输过程中
     *
     * @param future
     * @param progress
     * @param total
     * @throws Exception
     */
    @Override
    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {

    }

    /**
     * 传输完毕
     *
     * @param future
     * @throws Exception
     */
    @Override
    public void operationComplete(ChannelProgressiveFuture future) throws Exception {
        logger.info("文件[{}]传输结束.", fileName);
    }

}
