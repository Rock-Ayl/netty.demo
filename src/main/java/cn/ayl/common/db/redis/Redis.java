package cn.ayl.common.db.redis;

import cn.ayl.config.Const;

import cn.ayl.util.GsonUtils;
import cn.ayl.common.json.JsonObject;
import cn.ayl.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

/**
 * Redis-pool模式
 * create by Rock-Ayl 2019-6-12
 */
public class Redis {

    protected static Logger logger = LoggerFactory.getLogger(Redis.class);
    public static Redis user;
    public static Redis loginRecord;
    public static Redis auth;
    public static Redis oss;
    public static Redis facets;
    public static Redis facetId;

    protected String name;
    protected static JedisCluster jedisCluster = null;
    protected static JedisPool jedisPool;
    protected static boolean isPoolMode = true;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(1000);
        poolConfig.setMaxIdle(100);
        poolConfig.setTestOnBorrow(true);
        if (StringUtils.isEmpty(Const.RedisAuth)) {
            jedisPool = new JedisPool(poolConfig, Const.RedisHost, Const.RedisPort, Const.RedisTimeOut);
        } else {
            jedisPool = new JedisPool(poolConfig, Const.RedisHost, Const.RedisPort, Const.RedisTimeOut, Const.RedisAuth);
        }
        user = new Redis("user");
        auth = new Redis("auth");
        oss = new Redis("oss");
        facets = new Redis("facets");
        facetId = new Redis("facetId");
        loginRecord = new Redis("loginRecord");
    }

    public static Redis name(String name) {
        return new Redis(name);
    }

    public Redis(String name) {
        this.name = name;
    }

    protected String redisKey(String id) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(name)) {
            return id;
        } else {
            return name + "@" + id;
        }
    }


    public boolean set(String id, String content) {
        return this.setExpire(id, content, 0);
    }

    public boolean setExpire(String id, String content) {
        return this.setExpire(id, content, 10 * 24 * 3600);
    }

    public boolean setExpire(String id, String content, int seconds) {
        String key = redisKey(id);
        try {
            if (isPoolMode) {
                Jedis jedis = jedisPool.getResource();
                jedis.set(key, content);
                if (seconds > 0) {
                    jedis.expire(key, seconds);
                }
                jedis.close();
            } else {
                jedisCluster.set(key, content);
                if (seconds > 0) {
                    jedisCluster.expire(key, seconds);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("message", e);
            return false;
        }
    }

    public boolean setObject(String id, JsonObject value) {
        return this.setExpire(id, value.toJson(), 0);
    }

    public boolean setObjectExpire(String id, JsonObject value) {
        return this.setExpire(id, value.toJson(), 10 * 24 * 3600);
    }

    public boolean setObjectExpire(String id, JsonObject value, int seconds) {
        return this.setExpire(id, value.toJson(), seconds);
    }

    private String readContent(String key) {
        String content;
        if (isPoolMode) {
            Jedis jedis = jedisPool.getResource();
            content = jedis.get(key);
            jedis.close();
        } else {
            content = jedisCluster.get(key);
        }
        return content;
    }

    public JsonObject getObject(String id) {
        String key = redisKey(id);
        try {
            String content = readContent(key);
            if (org.apache.commons.lang3.StringUtils.isEmpty(content)) {
                return null;
            }
            return JsonUtils.parse(content);
        } catch (Exception e) {
            logger.error("message", e);
            return null;
        }
    }

    public String get(String id) {
        String key = redisKey(id);
        try {
            String content = readContent(key);
            return content;
        } catch (Exception e) {
            logger.error("message", e);
            return null;
        }
    }

    private void update(String key, String content) {
        if (isPoolMode) {
            Jedis jedis = jedisPool.getResource();
            jedis.set(key, content);
            jedis.close();
        } else {
            jedisCluster.set(key, content);
        }
    }

    public void updateObject(String id, JsonObject value) {
        String key = redisKey(id);
        JsonObject oJson;
        try {
            String content = readContent(key);
            if (!org.apache.commons.lang3.StringUtils.isEmpty(content)) {
                oJson = JsonUtils.parse(content);
            } else {
                oJson = JsonObject.VOID();
            }
            oJson.putAll(value);
            update(key, oJson.toJson());
        } catch (Exception e) {
            logger.error("updateObject", e);
        }
    }


    public void updateJson(String id, com.google.gson.JsonObject value) {
        String key = redisKey(id);
        com.google.gson.JsonObject oJson;
        try {
            String content = readContent(key);
            if (!org.apache.commons.lang3.StringUtils.isEmpty(content)) {
                oJson = GsonUtils.parse(content);
            } else {
                oJson = new com.google.gson.JsonObject();
            }
            oJson = GsonUtils.merge(oJson, value);
            update(key, oJson.toString());
        } catch (Exception e) {
            logger.error("message", e);
        }
    }

    public com.google.gson.JsonObject readJson(String id) {
        String redisKey = redisKey(id);
        com.google.gson.JsonObject oJson = null;
        try {
            String content = readContent(redisKey);
            if (!org.apache.commons.lang3.StringUtils.isEmpty(content)) {
                oJson = GsonUtils.parse(content);
            } else {
                oJson = new com.google.gson.JsonObject();
            }
        } catch (Exception e) {
            logger.error("message", e);
        }
        return oJson;
    }

    public void writeJson(String id, com.google.gson.JsonObject value) {
        String redisKey = redisKey(id);
        try {
            update(redisKey, value.toString());
        } catch (Exception e) {
            logger.error("message", e);
        }
    }

    public void update(String id, String key, String value) {
        com.google.gson.JsonObject oJson = readJson(id);
        oJson.addProperty(key, value);
        writeJson(id, oJson);
    }

    public void update(String id, String key, int value) {
        com.google.gson.JsonObject oJson = readJson(id);
        oJson.addProperty(key, value);
        writeJson(id, oJson);
    }

    public void update(String id, String key, long value) {
        com.google.gson.JsonObject oJson = readJson(id);
        oJson.addProperty(key, value);
        writeJson(id, oJson);
    }

    public void expire(String id, int seconds) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(id)) return;
        String key = redisKey(id);
        try {
            if (isPoolMode) {
                Jedis jedis = jedisPool.getResource();
                jedis.expire(key, seconds);
                jedis.close();
            } else {
                jedisCluster.expire(key, seconds);
            }
        } catch (Exception e) {
            logger.error("message", e);
        }
    }

    public void delete(String id) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(id)) return;
        String key = redisKey(id);
        try {
            if (isPoolMode) {
                Jedis jedis = jedisPool.getResource();
                jedis.del(key);
                jedis.close();
            } else {
                jedisCluster.del(key);
            }
        } catch (Exception e) {
            logger.error("message", e);
        }
    }

    public void writeCaptcha(String id, String code) {
        try {
            id = id + "-captcha";
            if (isPoolMode) {
                Jedis jedis = jedisPool.getResource();
                jedis.set(id, code);
                jedis.expire(id, 3 * 60);
                jedis.close();
            } else {
                jedisCluster.set(id, code);
                jedisCluster.expire(id, 3 * 60);
            }
        } catch (Exception e) {
            logger.error("message", e);
        }
    }

    public String readCaptcha(String id) {
        try {
            id = id + "-captcha";
            return readContent(id);
        } catch (Exception e) {
            logger.error("message", e);
            return null;
        }
    }

}
