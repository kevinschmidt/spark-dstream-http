package org.apache.spark.streaming.http

import java.net.URI
import java.util.concurrent.{ TimeUnit, ScheduledExecutorService, Executors }

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{ HttpClients, CloseableHttpClient }
import org.apache.http.util.EntityUtils
import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.util.Utils

import scala.concurrent.duration.FiniteDuration

class HttpInputDStream(
    @transient ssc_ : StreamingContext,
    uri: URI,
    interval: FiniteDuration,
    storageLevel: StorageLevel) extends ReceiverInputDStream[String](ssc_) with Logging {

  def getReceiver(): Receiver[String] = {
    new HttpReceiver(uri, interval, storageLevel)
  }
}

class HttpReceiver(
    url: URI,
    interval: FiniteDuration,
    storageLevel: StorageLevel) extends Receiver[String](storageLevel) with Logging {

  var httpClient: CloseableHttpClient = null
  var scheduledExecutorPool: ScheduledExecutorService = null

  def onStop() {
    if (scheduledExecutorPool != null) {
      scheduledExecutorPool.shutdown()
      scheduledExecutorPool = null
    }
    if (httpClient != null) {
      httpClient.close()
      httpClient = null
    }
  }

  def onStart() {
    logInfo("Starting HTTP Input Stream")
    httpClient = HttpClients.createDefault()
    try {
      scheduledExecutorPool = Executors.newScheduledThreadPool(1, Utils.namedThreadFactory("HttpInputHandler"))
      scheduledExecutorPool.scheduleAtFixedRate(new HttpHandler(url), interval.toMillis, interval.toMillis, TimeUnit.MILLISECONDS)
    } finally {
      scheduledExecutorPool.shutdown()
    }
  }

  private class HttpHandler(uri: URI) extends Runnable {
    val httpGet = new HttpGet(uri)

    def run(): Unit = {
      try {
        val result = EntityUtils.toString(httpClient.execute(httpGet).getEntity)
        store(result)
      } catch {
        case e: Throwable => logError("Error calling http get", e)
      }
    }
  }

}