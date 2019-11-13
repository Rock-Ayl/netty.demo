package cn.ayl.util;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * create by Rock-Ayl 2019-11-13
 * gitHub超轻量级Java类扫描器 工具
 */
public class ScanClassUtil {

    private static final Logger logger = LoggerFactory.getLogger(ScanClassUtil.class);

    public static ScanResult scan = new FastClasspathScanner().scan();

    public static List<String> readImplClassNames(String interFaceClassName) {
        return scan.getNamesOfClassesImplementing(interFaceClassName);
    }

    public static Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            return null;
        }
    }

    public static Class findImplClass(Class<?> interFaceClass) {
        List<String> implNames = ScanClassUtil.readImplClassNames(interFaceClass.getName());
        Class implClass = null;
        try {
            for (int t = 0; t < implNames.size(); t++) {
                String implClassName = implNames.get(t);
                implClass = Class.forName(implClassName);
            }
        } catch (Exception e) {
            logger.error("[{}]", interFaceClass.getName(), e);
            return null;
        }
        if (implClass == null) {
            logger.warn("[{}] has not implement class", interFaceClass.getName());
            return null;
        }
        return implClass;
    }


}
