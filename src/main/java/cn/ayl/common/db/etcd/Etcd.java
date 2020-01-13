package cn.ayl.common.db.etcd;

import mousio.etcd4j.EtcdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * created by Rock-Ayl on 2019-12-16
 * todo etcd连接池
 */
public class Etcd {

    protected static Logger logger = LoggerFactory.getLogger(Etcd.class);

    //连接
    public EtcdClient client;

    public Etcd() {
        client = new EtcdClient(URI.create("http://127.0.0.1:2379"));
    }

    //使用
    public static Etcd use() {
        return new Etcd();
    }

    //查看当前etcd服务版本
    public String getServerVersion() {
        return client.version().getServer();
    }

    //查看当前etcd集群版本
    public String getClusterVersion() {
        return client.version().getCluster();
    }

    public static void main(String[] args) {
        Etcd etcd = Etcd.use();
        logger.info("Etcd Server Version : {}", etcd.getServerVersion());
        logger.info("Etcd Cluster Version : {}", etcd.getClusterVersion());
        System.exit(-1);

    }
}
