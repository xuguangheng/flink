package guanghxu;

import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.io.IOException;

public class KafkaUpsertModeTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStateBackend(getStateBackend());
        env.enableCheckpointing(10000);
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(env, settings);

        tableEnvironment.executeSql(" CREATE TABLE kafka (" +
                "   user_id STRING," +
                "   name STRING," +
                "   PRIMARY KEY (user_id) NOT ENFORCED" +
                " ) WITH (" +
                "   'connector' = 'upsert-kafka'," +
                "   'topic' = 'kafka_checkpoint_test'," +
                "   'properties.bootstrap.servers' = 'b-1.test.i0fd1p.c2.kafka.us-east-1.amazonaws.com:9094,b-3.test.i0fd1p.c2.kafka.us-east-1.amazonaws.com:9094'," +
                "   'properties.security.protocol' = 'SSL'," +
                "   'properties.ssl.truststore.location' = '/usr/lib/jvm/java-1.8.0-amazon-corretto.x86_64/jre/lib/security/cacerts'," +
                "   'key.format' = 'json'," +
                "   'value.format' = 'json'," +
                "   'value.fields-include' = 'EXCEPT_KEY'" +
                " )");

        tableEnvironment.executeSql("CREATE TABLE es (" +
                "   user_id STRING," +
                "   name STRING," +
                "   PRIMARY KEY (user_id) NOT ENFORCED" +
                " ) WITH (" +
                "  'connector' = 'elasticsearch-7'," +
                "  'hosts' = 'https://vpc-change-mode-test-6w5qoxxbt2oy6vf4jegayvdwqm.us-east-1.es.amazonaws.com:443'," +
                "  'index' = 'kafka_test'" +
                ")");

        tableEnvironment.executeSql("INSERT INTO es SELECT * from kafka").print();
    }

    private static StateBackend getStateBackend() throws IOException {
        final String stateBackendPath = "s3://guanghxu-flink-test/state_backend/";
        return new RocksDBStateBackend(stateBackendPath);
    }

}
