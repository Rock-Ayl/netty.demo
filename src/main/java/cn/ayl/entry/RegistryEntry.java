package cn.ayl.entry;

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
            for (String className : names) {
                //获取类
                Class cls = ScanClassUtil.scan.classNameToClassRef(className);
                //new 服务实体
                ServiceEntry serviceEntry = new ServiceEntry(cls);
                //初始化服务实体
                serviceEntry.init();
                //组装至List存放
                serviceMap.put(className, serviceEntry);
            }
        }
        logger.info(">>>>>> RegistryEntry Scan All Services >>>>>>");
    }

}
