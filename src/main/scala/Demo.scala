//package guanghxu
//
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
//import org.apache.flink.table.api.EnvironmentSettings
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
//
//
//object Demo {
//  def main(args: Array[String]): Unit = {
//    val environment = StreamExecutionEnvironment.getExecutionEnvironment
//    val settings = EnvironmentSettings.newInstance.useBlinkPlanner.inStreamingMode.build
//    val tableEnvironment = StreamTableEnvironment.create(environment, settings)
//
//    tableEnvironment.executeSql("CREATE TABLE UserScores_source (id INT, name STRING)" + " WITH (" + "'connector' = 'socket'," + "'hostname' = 'localhost'," + "'port' = '9999'," + "'byte-delimiter' = '10'," + "'format' = 'changelog-csv'," + "'changelog-csv.column-delimiter' = '|'" + ")")
//    tableEnvironment.executeSql("CREATE VIEW UserScores AS (" + "select id, max(name) as name from UserScores_source group by id)")
//    tableEnvironment.executeSql("CREATE TABLE UserName_source (id INT, name STRING)" + " WITH (" + "'connector' = 'socket'," + "'hostname' = 'localhost'," + "'port' = '1111'," + "'byte-delimiter' = '10'," + "'format' = 'changelog-csv'," + "'changelog-csv.column-delimiter' = '|'" + ")")
//    tableEnvironment.executeSql("CREATE VIEW UserName AS (" + "select id, max(name) as name from UserName_source group by id)")
//    //        tableEnvironment.executeSql("CREATE TABLE myUserTable (Lid INT, Lname STRING, id INT, name STRING)" +
//    //                " WITH (" +
//    //                "  'connector' = 'elasticsearch-7'," +
//    //                "  'hosts' = 'https://vpc-change-mode-test-6w5qoxxbt2oy6vf4jegayvdwqm.us-east-1.es.amazonaws.com:443'," +
//    //                "  'index' = 'users'" +
//    //                ")");
//    tableEnvironment.executeSql("select UserScores.id as Lid, UserScores.name as Lname, UserName.id, UserName.name from UserScores join UserName on UserName.id = UserScores.id").print()
//  }
//}
