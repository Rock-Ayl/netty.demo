package cn.ayl.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.function.Function;

/**
 * create by Rock-Ayl 2019-6-13
 * 操作properties的工具类
 */
public class PropertyFileUtil extends Properties {

    private static final Logger logger = LoggerFactory.getLogger(PropertyFileUtil.class);

    public PropertyFileUtil() {
        defaults = new Properties();
    }

    public PropertyFileUtil use(String fileName) {
        try {
            InputStream stream = PropertyFileUtil.class.getResourceAsStream("/" + fileName);
            InputStreamReader in = new InputStreamReader(stream, "UTF-8");
            defaults.load(in);
            in.close();
        } catch (IOException e) {
            logger.error(fileName, e);
        }
        return this;
    }

    public String asString(String name, String defaultValue) {
        return getOrDefault(name, defaultValue, String::toString);
    }

    public int asInt(String name, int defaultValue) {
        return getOrDefault(name, defaultValue, Integer::parseInt);
    }

    public boolean asBool(String name, boolean defaultValue) {
        return getOrDefault(name, defaultValue, Boolean::parseBoolean);
    }

    public <R> R get(String key, Function<String, R> f) {
        String value = defaults.getProperty(key);
        R result = f.apply(value);
        return result;
    }

    public <R> R getOrDefault(String key, R defaultValue, Function<String, R> f) {
        String value = defaults.getProperty(key);
        String resultValue = value == null ? defaultValue.toString() : value;
        R result = f.apply(resultValue);
        return result;
    }

}
