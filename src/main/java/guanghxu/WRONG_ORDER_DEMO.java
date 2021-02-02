package guanghxu;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class WRONG_ORDER_DEMO {
    public static void main(String[] args) {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(environment, settings);

        tableEnvironment.executeSql("CREATE TABLE A (asin STRING, title STRING, primary key(asin) NOT ENFORCED)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '9999'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");

        tableEnvironment.executeSql("CREATE TABLE B (asin STRING, fnsku STRING, age STRING, primary key(asin) NOT ENFORCED)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '1111'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");









//        tableEnvironment.executeSql("SELECT marketplace FROM asin_marketplace_source GROUP BY marketplace").print();
    }
}

