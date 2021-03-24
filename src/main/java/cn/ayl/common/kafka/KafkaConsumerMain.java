package cn.ayl.common.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created By Rock-Ayl on 2021-03-24
 * kafka 消费者 demo
 */
public class KafkaConsumerMain {

    public static void main(String[] args) {
        Properties props = new Properties();
        // kafka 服务器
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        // 组名 不同组名可以重复消费。例如你先使用了组名A消费了kafka的1000条数据，但是你还想再次进行消费这1000条数据，并且不想重新去产生，那么这里你只需要更改组名就可以重复消费了
        props.setProperty("group.id", "test");
        // 是否自动提交，默认为true
        props.setProperty("enable.auto.commit", "true");
        // 从poll(拉)的回话处理时长
        props.setProperty("auto.commit.interval.ms", "1000");
        // 键序列化，默认org.apache.kafka.common.serialization.StringDeserializer
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // 键序列化，默认org.apache.kafka.common.serialization.StringDeserializer
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("my-topic"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }
}
