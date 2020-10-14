package cn.ayl.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created By Rock-Ayl on 2020-10-14
 * RSA工具包
 * 0.生成秘钥
 * 1.加密
 * 2.解密
 */
public class RSAUtils {

    protected static Logger logger = LoggerFactory.getLogger(RSAUtils.class);

    //RSA-公钥
    private static String PublicKey;
    //RSA-私钥
    private static String PrivateKey;

    //初始化
    static {
        try {
            //KeyPairGenerator类用于生成公钥和私钥对,基于RSA算法生成对象
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            //初始化密钥对生成器，密钥大小为96-1024位
            keyPairGen.initialize(512, new SecureRandom());
            //生成一对密钥
            KeyPair keyPair = keyPairGen.generateKeyPair();
            //公钥存储
            PublicKey = new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()));
            //私钥存储
            PrivateKey = new String(Base64.encodeBase64((keyPair.getPrivate().getEncoded())));
        } catch (NoSuchAlgorithmException e) {
            logger.error("RSA生成秘钥异常:[{}]", e);
        }
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKey))));
        return Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey))));
        return new String(cipher.doFinal(Base64.decodeBase64(str.getBytes("UTF-8"))));
    }

    //测试
    public static void main(String[] args) throws Exception {
        //加密字符串
        String message = "df723820";
        logger.info("随机生成的公钥为:" + PublicKey);
        logger.info("随机生成的私钥为:" + PrivateKey);
        String messageEn = encrypt(message, PublicKey);
        logger.info("加密前:" + message);
        logger.info("加密后:" + messageEn);
        String messageDe = decrypt(messageEn, PrivateKey);
        logger.info("还原后:" + messageDe);
    }

}
