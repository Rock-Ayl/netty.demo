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
public class RegistryEntry {

    protected static Logger logger = LoggerFactory.getLogger(RegistryEntry.class);

    //主要功能
    public static List<String> DefaultService = init();
    //所有服务
    public static ConcurrentHashMap<String, ServiceEntry> serviceMap = new ConcurrentHashMap();

    /**
     * 初始化功能列表
     */
    private static List<String> init() {
        //创建
        List<String> function = new ArrayList<>();
        //组装 webSocket
        function.add(Const.WebSocketPath.replace('/', '.'));
        //组装 upload
        function.add(Const.UploadPath.replace('/', '.'));
        //组装 download
        function.add(Const.DownloadPath.replace('/', '.'));
        //返回
        return function;
    }

    //扫描所有服务
    public static void scanServices() {
        //获取所有继承 IMicroService的接口路径
        List<String> names = ScanClassUtils.scan.getNamesOfSubinterfacesOf(IMicroService.class);
        //判空
        if (CollectionUtils.isEmpty(names)) {
            //提示下
            logger.info(">>>>>> 不存在任何服务 >>>>>>");
            return;
        }
        //获取当前服务
        for (String className : names) {
            //循环系统功能
            for (String otherService : DefaultService) {
                //如果默认功能被服务占用
                if (className.endsWith(otherService)) {
                    //报错
                    logger.error("默认服务被占用,被占用者:" + otherService + "占用者:" + className + ",请修改服务名.");
                    //强制停止系统
                    System.exit(-1);
                }
            }
            //获取类
            Class cls = ScanClassUtils.scan.classNameToClassRef(className);
            //获取实现类
            List<String> implNames = ScanClassUtils.readImplClassNames(cls.getName());
            //没有实现不注册
            if (CollectionUtils.isEmpty(implNames)) {
                //提示
                logger.error("[{}] has not implement class", cls.getName());
                continue;
            }
            //创建服务实体
            ServiceEntry serviceEntry = new ServiceEntry(cls);
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
