package cn.ayl.common.db.etcd;

import cn.ayl.config.Const;
import mousio.etcd4j.EtcdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * created by Rock-Ayl on 2019-12-16
 * etcd-单机
 */
public class Etcd {

    protected static Logger logger = LoggerFactory.getLogger(Etcd.class);

    //连接
    public EtcdClient Client;

    //私有
    private Etcd() {
        Client = new EtcdClient(URI.create("http://" + Const.EtcdHost + ":" + Const.EtcdPort));
    }

    /**
     * 默认使用
     *
     * @return
     */
    public static Etcd use() {
        return new Etcd();
    }

    /**
     * 查看当前etcd服务版本
     *
     * @return
     */
    public String getServerVersion() {
        return Client.version().getServer();
    }

    /**
     * 查看当前etcd集群版本
     *
     * @return
     */
    public String getClusterVersion() {
        return Client.version().getCluster();
    }

}
