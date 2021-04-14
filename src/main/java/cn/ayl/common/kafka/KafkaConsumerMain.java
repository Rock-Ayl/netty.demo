package cn.ayl.common.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created By Rock-Ayl on 2021-03-24
 * kafka 自身的消费者 demo
 */
public class KafkaConsumerMain {

    /**
     * 获取kafka消费者配置
     *
     * @return
     */
    public static Properties getProperties(String group) {
        //初始化
        Properties props = new Properties();
        // kafka 服务器
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        // 组名 不同组名可以重复消费。例如你先使用了组名A消费了kafka的1000条数据，但是你还想再次进行消费这1000条数据，并且不想重新去产生，那么这里你只需要更改组名就可以重复消费了
        props.setProperty("group.id", group);
        // 是否自动提交，默认为true
        props.setProperty("enable.auto.commit", "true");
        // 从poll(拉)的回话处理时长
        props.setProperty("auto.commit.interval.ms", "1000");
        // 键序列化，默认org.apache.kafka.common.serialization.StringDeserializer
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // 键序列化，默认org.apache.kafka.common.serialization.StringDeserializer
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //返回
        return props;
    }

    public static void main(String[] args) {
        //初始化kafka消费者,载入配置
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(getProperties("kafka-consumer"));
        //订阅主题List
        consumer.subscribe(Arrays.asList(KafkaProducerMain.Topic));
        //循环
        while (true) {
            //获取消费数据
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            //循环
            for (ConsumerRecord<String, String> record : records) {
                //输出
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }

}
