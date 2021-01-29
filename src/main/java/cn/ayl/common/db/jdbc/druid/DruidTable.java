package cn.ayl.common.db.jdbc.druid;

import cn.ayl.config.Const;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created By Rock-Ayl on 2020-12-18
 * 阿里-Druid-连接池
 */
public class DruidTable {

    protected static Logger logger = LoggerFactory.getLogger(DruidTable.class);

    /**
     * 创建Mysql数据源
     *
     * @param host                          主机
     * @param port                          端口
     * @param dbName                        数据库名称
     * @param encodingType                  编码类型
     * @param userName                      账户名
     * @param password                      密码
     * @param initialSize                   初始化连接数
     * @param maxActive                     最大活动连接数
     * @param maxWait                       连接时最大等待时间
     * @param testWhileIdle                 申请链接时是否检测
     * @param timeBetweenEvictionRunsMillis testWhileIdle的判断依据
     * @param minEvictableIdleTimeMillis    最小空闲时间
     * @param maxEvictableIdleTimeMillis    最大空闲时间
     * @param testOnBorrow                  申请连接时执行validationQuery检测连接是否有效
     * @param validationQuery               用来检测连接有效的语句
     * @param validationQueryTimeout        检测连接超时时间
     * @param testOnReturn                  归还连接时执行validationQuery检测连接是否有效
     * @param poolPreparedStatements        是否缓存preparedStatement
     * @param maxOpenPreparedStatements     要启用PSCache的配置数值
     * @return
     */
    public static DruidDataSource createMySqlDataSource(String host, String port, String dbName, DruidMysqlEncodingType encodingType, String userName, String password, int initialSize, int maxActive, int maxWait, boolean testWhileIdle, int timeBetweenEvictionRunsMillis, int minEvictableIdleTimeMillis, int maxEvictableIdleTimeMillis, boolean testOnBorrow, String validationQuery, int validationQueryTimeout, boolean testOnReturn, boolean poolPreparedStatements, int maxOpenPreparedStatements) {
        //创建
        DruidDataSource druid = new DruidDataSource();
        //数据库为Mysql
        druid.setDriverClassName(Const.JdbcDriver);
        //url
        druid.setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useUnicode=true&characterEncoding=" + encodingType.toChinese() + "&useSSL=false");
        //账户
        druid.setUsername(userName);
        //密码判空
        if (StringUtils.isNotBlank(password)) {
            //密码
            druid.setPassword(password);
        }
        //初始化连接数
        druid.setInitialSize(initialSize);
        //最大活动连接池数量
        druid.setMaxActive(maxActive);
        //连接时最大等待时间,单位毫秒
        druid.setMaxWait(maxWait);
        //建议配置true,不影响性能,保证安全性.申请连接的时候检测,如果空闲时间大于timeBetweenEvictionRunsMillis,执行validationQuery检测连接是否有效.
        druid.setTestWhileIdle(testWhileIdle);
        //Destroy线程会检测连接的间隔时间,testWhileIdle的判断依据
        druid.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        //最小空闲时间
        druid.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        //最大空闲时间
        druid.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
        //申请连接时执行validationQuery检测连接是否有效,开启降低性能
        druid.setTestOnBorrow(testOnBorrow);
        //用来检测连接是否有效的sql
        druid.setValidationQuery(validationQuery);
        //检测连接超时时间
        druid.setValidationQueryTimeout(validationQueryTimeout);
        //归还连接时执行validationQuery检测连接是否有效,开启降低性能
        druid.setTestOnReturn(testOnReturn);
        //是否缓存preparedStatement,也就是PSCache,PSCache对支持游标的数据库性能提升巨大,比如说oracle,在mysql5.5以下的版本中没有PSCache功能,建议关闭掉.5.5及以上版本有PSCache,建议开启.
        druid.setPoolPreparedStatements(poolPreparedStatements);
        //要启用PSCache,必须配置大于0,当大于0时,poolPreparedStatements自动触发修改为true,不会存在Oracle下PSCache占用内存过多的问题,可以把这个数值配置大一些,比如说100
        druid.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
        //返回
        return druid;
    }

}
