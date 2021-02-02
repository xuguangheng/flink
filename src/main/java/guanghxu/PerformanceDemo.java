package guanghxu;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class PerformanceDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(environment, settings);

        tableEnvironment.executeSql("CREATE TABLE A (asin STRING, fnsku STRING)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '9999'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");

        tableEnvironment.executeSql("CREATE TABLE B (asin STRING, fnsku STRING)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '1111'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");

        tableEnvironment.executeSql("CREATE TABLE C (fnsku STRING)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '1111'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");


        tableEnvironment.executeSql("CREATE VIEW D AS select A.fnsku from A join B on A.asin = B.asin");
        tableEnvironment.executeSql("SELECT C.fnsku from C join D on C.fnsku = D.fnsku");


//        tableEnvironment.executeSql("SELECT marketplace FROM asin_marketplace_source GROUP BY marketplace").print();
    }
}

