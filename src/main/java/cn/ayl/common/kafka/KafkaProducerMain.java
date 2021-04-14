package cn.ayl.common.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created By Rock-Ayl on 2021-03-24
 * kafka 生产者 demo
 */
public class KafkaProducerMain {

    //主题
    public final static String Topic = "my-topic";

    /**
     * 获取kafka生产者配置
     *
     * @return
     */
    public static Properties getProperties() {
        //初始化
        Properties pro = new Properties();
        // kafka服务器
        pro.put("bootstrap.servers", "127.0.0.1:9092");
        // 这个配置意味着leader会等待所有的follower同步完成。这个确保消息不会丢失，除非kafka集群中所有机器挂掉。这是最强的可用性保证
        pro.put("acks", "all");
        // 配置为大于0的值的话，客户端会在消息发送失败时重新发送
        pro.put("retries", 0);
        pro.put("linger.ms", 1);
        // 配置为大于0的值的话，客户端会在消息发送失败时重新发送。
        pro.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 值序列化，默认org.apache.kafka.common.serialization.StringDeserializer
        pro.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //返回
        return pro;
    }

    public static void main(String[] args) {
        //初始化生产者,载入配置
        Producer<String, String> producer = new KafkaProducer<>(getProperties());
        //一个线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    //key、value
                    String key = Integer.toString(i);
                    String value = Integer.toString(i);
                    //不停的发送
                    producer.send(new ProducerRecord<String, String>(KafkaProducerMain.Topic, key, value));
                    System.out.println("发送成功！" + i++);
                    try {
                        //等待一下
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //启动线程
        thread.start();
    }

}