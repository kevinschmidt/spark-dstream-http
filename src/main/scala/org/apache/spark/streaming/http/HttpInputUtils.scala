package org.apache.spark.streaming.http

import java.net.URI

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream

import scala.concurrent.duration.FiniteDuration

object HttpInputUtils {
  def createStream(
    ssc: StreamingContext,
    uri: URI,
    interval: FiniteDuration,
    storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2): ReceiverInputDStream[String] = {
    new HttpInputDStream(ssc, uri, interval, storageLevel)
  }
}
