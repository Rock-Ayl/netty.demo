package cn.ayl.entry;

import cn.ayl.config.Const;
import cn.ayl.intf.IMicroService;
import cn.ayl.util.ScanClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Created by Rock-Ayl on 2019-11-13
 * 所有服务的注册表
 */
public class RegistryEntry {

    protected static Logger logger = LoggerFactory.getLogger(RegistryEntry.class);

    //存放所有服务
    public static ConcurrentHashMap<String, ServiceEntry> serviceMap = new ConcurrentHashMap();

    //扫描所有服务
    public static void scanServices() {
        //获取所有继承 IMicroService的接口路径
        List<String> names = ScanClassUtil.scan.getNamesOfSubinterfacesOf(IMicroService.class);
        if (names.size() > 0) {
            //获取当前服务
            for (String className : names) {
                //循环所有默认服务
                for (String otherService : Const.DefaultService) {
                    //如果默认服务被占用
                    if (className.endsWith(otherService)) {
                        logger.error("默认服务被占用,被占用者:" + otherService + "占用者:" + className);
                        //强制停止系统
                        System.exit(-1);
                    }
                }
                //获取类
                Class cls = ScanClassUtil.scan.classNameToClassRef(className);
                //获取实现类
                List<String> implNames = ScanClassUtil.readImplClassNames(cls.getName());
                //没有实现不注册
                if (implNames.size() == 0) {
                    logger.error("[{}] has not implement class", cls.getName());
                    continue;
                }
                //创建服务实体
                ServiceEntry serviceEntry = new ServiceEntry(cls);
                //初始化服务实体，解析里面的方法、参数
                serviceEntry.init();
                //组装至List存放
                serviceMap.put(cls.getSimpleName(), serviceEntry);
                logger.info("[{}] Register Success", cls.getName());
            }
        }
        logger.info(">>>>>> RegistryEntry Scan All Services >>>>>>");
    }

}
