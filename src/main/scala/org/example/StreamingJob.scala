/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment

/**
 * Skeleton for a Flink Streaming Job.
 *
 * For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
object StreamingJob {
  def main(args: Array[String]) {
    val environment = StreamExecutionEnvironment.getExecutionEnvironment
    val settings = EnvironmentSettings.newInstance.useBlinkPlanner().inStreamingMode.build
    val tableEnvironment = StreamTableEnvironment.create(environment, settings)


    // Stream

    // SQL
    tableEnvironment.executeSql("""
        CREATE TABLE UserScores_source (id INT, name STRING)
        WITH (
          'connector' = 'socket',
          'hostname' = 'localhost',
          'port' = '9999',
          'byte-delimiter' = '10',
          'format' = 'changelog-csv',
          'changelog-csv.column-delimiter' = '|'
        )""")

    tableEnvironment.executeSql("""
                                   CREATE VIEW UserScores AS (
                                   select id, max(name) as name from UserScores_source group by id)
                                   """).print()

    tableEnvironment.executeSql("""
        CREATE TABLE UserName_source (id INT, name STRING)
        WITH (
          'connector' = 'socket',
          'hostname' = 'localhost',
          'port' = '1111',
          'byte-delimiter' = '10',
          'format' = 'changelog-csv',
          'changelog-csv.column-delimiter' = '|'
        )""")
    tableEnvironment.executeSql("""
                                   CREATE VIEW UserName AS (
                                   select id, max(name) as name from UserName_source group by id)
                                   """).print()
    tableEnvironment.executeSql("""
                                 select * from UserScores
                                 """).print()


        tableEnvironment.executeSql("""
        CREATE TABLE UserName (id INT, name STRING, PRIMARY KEY(id) NOT ENFORCED)
        WITH (
          'connector' = 'socket',
          'hostname' = 'localhost',
          'port' = '1111',
          'byte-delimiter' = '10',
          'format' = 'changelog-csv',
          'changelog-csv.column-delimiter' = '|'
        )""")


    tableEnvironment.executeSql(
      """
         SELECT UserName.id as LID, UserName.name as LName, UserScores.id, UserScores. name
         FROM UserName
         JOIN UserScores
         ON UserScores.name = UserName.name
      """
    ).print()

  }
}
