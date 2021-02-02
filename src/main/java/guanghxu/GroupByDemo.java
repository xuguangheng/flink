package guanghxu;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class GroupByDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(environment, settings);

        tableEnvironment.executeSql("CREATE TABLE A (name STRING, scores INT, title STRING)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '9999'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");

        tableEnvironment.executeSql("CREATE TABLE B (name STRING, scores STRING)" +
                " WITH (" +
                "'connector' = 'socket'," +
                "'hostname' = 'localhost'," +
                "'port' = '1111'," +
                "'byte-delimiter' = '10'," +
                "'format' = 'changelog-csv'," +
                "'changelog-csv.column-delimiter' = '|'" +
                ")");

//        tableEnvironment.executeSql("SELECT name, scores FROM A group by name, scores").print();
//        tableEnvironment.executeSql("SELECT name, scores FROM A group by name, scores").print();

//        tableEnvironment.executeSql("CREATE VIEW C AS SELECT name, max(scores) as scores FROM A group by name, title");


        tableEnvironment.executeSql("SELECT name, max(scores) as scores from B group by name").print();



//        tableEnvironment.executeSql("SELECT marketplace FROM asin_marketplace_source GROUP BY marketplace").print();
    }
}

