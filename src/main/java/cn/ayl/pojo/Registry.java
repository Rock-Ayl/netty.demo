package cn.ayl.pojo;

import cn.ayl.config.Const;
import cn.ayl.intf.IMicroService;
import cn.ayl.util.ScanClassUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Created by Rock-Ayl on 2019-11-13
 * 所有服务的注册表
 */
public class Registry {

    protected static Logger logger = LoggerFactory.getLogger(Registry.class);

    //默认服务
    public static List<String> DefaultServiceList = init();
    //所有服务
    public static ConcurrentHashMap<String, Service> serviceMap = new ConcurrentHashMap();

    /**
     * 初始化默认服务列表
     */
    private static List<String> init() {
        //创建
        List<String> functionSet = new ArrayList<>();
        //组装 webSocket
        functionSet.add(Const.WebSocketPath.replace('/', '.'));
        //组装 upload
        functionSet.add(Const.UploadPath.replace('/', '.'));
        //组装 download
        functionSet.add(Const.DownloadPath.replace('/', '.'));
        //返回
        return functionSet;
    }

    //扫描所有服务
    public static void scanServices() {
        //获取所有继承 IMicroService的接口路径
        List<String> serviceList = ScanClassUtils.scan.getNamesOfSubinterfacesOf(IMicroService.class);
        //判空
        if (CollectionUtils.isEmpty(serviceList)) {
            //提示下
            logger.info(">>>>>> 不存在任何服务 >>>>>>");
            return;
        }
        //获取当前服务
        for (String serviceName : serviceList) {
            //默认
            for (String defaultServiceName : DefaultServiceList) {
                //判断该服务有没有被注册过
                if (serviceName.endsWith(defaultServiceName)) {
                    logger.error("默认服务被占用,被占用者:" + defaultServiceName + "占用者:" + serviceName + ",请修改服务名.");
                    //强制停止系统
                    System.exit(-1);
                }
            }
            //获取类
            Class cls = ScanClassUtils.scan.classNameToClassRef(serviceName);
            //获取实现类
            List<String> implNames = ScanClassUtils.readImplClassNames(cls.getName());
            //没有实现不注册
            if (CollectionUtils.isEmpty(implNames)) {
                //提示
                logger.error("[{}] has not implement class", cls.getName());
                continue;
            }
            //创建服务实体
            Service serviceEntry = new Service(cls);
            //初始化服务实体，解析里面的方法、参数
            serviceEntry.init();
            //组装至List存放
            serviceMap.put(cls.getSimpleName(), serviceEntry);
            //日志
            logger.info("[{}] Register Success", cls.getName());
        }
        logger.info(">>>>>> RegistryEntry Scan All Services >>>>>>");
    }

}
