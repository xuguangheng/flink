package guanghxu;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Demo {
    public static void main(String[] args) {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(environment, settings);

        tableEnvironment.executeSql("CREATE TABLE combined_v2_source (" +
                "  marketplaceId BIGINT," +
                "  asin STRING," +
                "  merchantId BIGINT," +
                "  msku STRING," +
                "  fnsku STRING," +
                "  payload STRING" +
                ") WITH (" +
                " 'connector' = 'upsert-kinesis'," +
                " 'stream' = 'combinedv2-kinesis-demo'," +
                " 'aws.credentials.provider' = 'ASSUME_ROLE'," +
                " 'aws.credentials.role.arn' = 'arn:aws:iam::318889136660:role/KinesisPolicyForCrossAccount'," +
                " 'aws.credentials.role.sessionName' = 'TempSessionForCombinedV2'," +
                " 'aws.credentials.role.externalId' = 'iCatDemo'," +
                " 'aws.region' = 'us-east-1'," +
                " 'scan.stream.initpos' = 'LATEST'," +
                " 'format' = 'sable-json'" +
                ")");

        tableEnvironment.executeSql("CREATE VIEW combined_v2 AS (" +
                "SELECT marketplaceId, asin, merchantId, msku, fnsku, max(payload) as combined_payload " +
                "FROM combined_v2_source " +
                "GROUP BY marketplaceId, asin, merchantId, msku, fnsku)");

        tableEnvironment.executeSql("CREATE TABLE excess_v2_source (" +
                "  marketplaceId BIGINT," +
                "  asin STRING," +
                "  merchantId BIGINT," +
                "  msku STRING," +
                "  payload STRING" +
                ") WITH (" +
                " 'connector' = 'upsert-kinesis'," +
                " 'stream' = 'AFNInvExcessMetricsV2-demo'," +
                " 'aws.credentials.provider' = 'ASSUME_ROLE'," +
                " 'aws.credentials.role.arn' = 'arn:aws:iam::318889136660:role/KinesisPolicyForCrossAccount'," +
                " 'aws.credentials.role.sessionName' = 'TempSessionForExcessV2'," +
                " 'aws.credentials.role.externalId' = 'iCatDemo'," +
                " 'aws.region' = 'us-east-1'," +
                " 'scan.stream.initpos' = 'LATEST'," +
                " 'format' = 'sable-json'" +
                ")");

        tableEnvironment.executeSql("CREATE VIEW excess_v2 AS (" +
                "SELECT marketplaceId, asin, merchantId, msku, max(payload) as excess_payload " +
                "FROM excess_v2_source " +
                "GROUP BY marketplaceId, asin, merchantId, msku)");

        tableEnvironment.executeSql("CREATE TABLE combine_excess_join_test (" +
                " marketplaceId BIGINT," +
                " asin STRING," +
                " merchantId BIGINT," +
                " msku STRING," +
                " fnsku STRING," +
                " combined_payload STRING," +
                " excess_payload STRING," +
                "PRIMARY KEY(marketplaceId, asin, merchantId, msku, fnsku) NOT ENFORCED)" +
                " WITH (" +
                "  'connector' = 'elasticsearch-7'," +
                "  'hosts' = 'https://vpc-change-mode-test-6w5qoxxbt2oy6vf4jegayvdwqm.us-east-1.es.amazonaws.com:443'," +
                "  'index' = 'combine_excess'" +
                ")");


        tableEnvironment.executeSql("INSERT INTO combine_excess_join_test " +
                "SELECT " +
                "combined_v2.marketplaceId as marketplaceId," +
                "combined_v2.asin as asin," +
                "combined_v2.merchantId as merchantId," +
                "combined_v2.msku as msku," +
                "fnsku," +
                "excess_payload," +
                "combined_payload" +
                "FROM combined_v2 JOIN excess_v2 " +
                "ON combined_v2.marketplaceId = excess_v2.marketplaceId" +
                "AND combined_v2.asin = excess_v2.asin" +
                "AND combined_v2.merchantId = excess_v2.merchantId" +
                "AND combined_v2.msku = excess_v2.msku");
    }
}
