/*
 * Copyright 2012 Twitter Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.twitter.zipkin.builder.ZooKeeperClientBuilder
import com.twitter.zipkin.cassandra
import com.twitter.zipkin.config._
import com.twitter.logging.LoggerFactory
import com.twitter.logging.config._
import com.twitter.ostrich.admin.{TimeSeriesCollectorFactory, JsonStatsLoggerFactory, StatsFactory}
import com.twitter.zipkin.storage.Store

// development mode.
new ZipkinQueryConfig {

  serverPort = 9411
  adminPort  = 9901

  adminStatsNodes =
    StatsFactory(
      reporters = JsonStatsLoggerFactory(
        loggerName = "stats",
        serviceName = "zipkin-query"
      ) :: new TimeSeriesCollectorFactory
    )

  val keyspaceBuilder = cassandra.Keyspace.static()
  def storeBuilder = Store.Builder(cassandra.StorageBuilder(keyspaceBuilder), cassandra.IndexBuilder(keyspaceBuilder), cassandra.AggregatesBuilder(keyspaceBuilder))

  def zkClientBuilder = ZooKeeperClientBuilder(Seq("localhost"))

  loggers =
    LoggerFactory (
      level = Level.DEBUG,
      handlers =
        new FileHandlerConfig {
          filename = "zipkin-query.log"
          roll = Policy.SigHup
        } ::
          new ConsoleHandlerConfig
    ) :: LoggerFactory (
      node = "stats",
      level = Level.INFO,
      useParents = false,
      handlers = new FileHandlerConfig {
        filename = "stats.log"
        formatter = BareFormatterConfig
      }
    )
}
