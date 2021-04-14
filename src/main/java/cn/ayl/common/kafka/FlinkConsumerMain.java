package cn.ayl.common.kafka;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

/**
 * Created By Rock-Ayl on 2021-04-14
 * Flink 消费 kafka demo
 */
public class FlinkConsumerMain {

    public static void main(String[] args) throws Exception {
        //获取环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //载入配置,订阅主题
        FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<String>(KafkaProducerMain.Topic, new SimpleStringSchema(), KafkaConsumerMain.getProperties());
        //获取数据
        DataStream<String> text = env.addSource(myConsumer);
        //打印
        text.print().setParallelism(1);
        //实现
        env.execute();
    }

}
