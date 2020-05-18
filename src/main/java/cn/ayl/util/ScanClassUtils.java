package cn.ayl.util;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * create by Rock-Ayl 2019-11-13
 * FastClasspathScanner是一个超轻量的类路径的扫描程序.
 */
public class ScanClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ScanClassUtils.class);

    public static ScanResult scan = new FastClasspathScanner().scan();

    public static List<String> readImplClassNames(String interFaceClassName) {
        return scan.getNamesOfClassesImplementing(interFaceClassName);
    }

    /**
     * 根据class名获取class
     *
     * @param className
     * @return
     */
    public static Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            logger.warn("no class :[{}] ", className);
            return null;
        }
    }

    /**
     * 找到接口的实现类
     *
     * @param interFaceClass
     * @return
     */
    public static Class findImplClass(Class<?> interFaceClass) {
        //扫描出所有实现类
        List<String> implNames = ScanClassUtils.readImplClassNames(interFaceClass.getName());
        //判空
        if (CollectionUtils.isEmpty(implNames)) {
            logger.warn("[{}] has no implement", interFaceClass.getName());
            return null;
        }
        //获取第一个实现类,其他抛弃
        Class implClass = getClass(implNames.get(0));
        //判空
        if (implClass == null) {
            logger.warn("[{}] has not implement class", interFaceClass.getName());
            return null;
        }
        return implClass;
    }

}
