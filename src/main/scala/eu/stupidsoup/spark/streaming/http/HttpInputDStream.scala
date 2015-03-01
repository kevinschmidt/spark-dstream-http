package eu.stupidsoup.spark.streaming.http

import java.net.URL

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

class HttpInputDStream[K: ClassTag, V: ClassTag](
    @transient ssc_ : StreamingContext,
    url: URL,
    interval: FiniteDuration,
    storageLevel: StorageLevel) extends ReceiverInputDStream[(K, V)](ssc_) with Logging {

  def getReceiver(): Receiver[(K, V)] = {
    new HttpReceiver[K, V](url, interval, storageLevel)
  }
}

class HttpReceiver[K: ClassTag, V: ClassTag](
    url: URL,
    interval: FiniteDuration,
    storageLevel: StorageLevel) extends Receiver[(K, V)](storageLevel) with Logging {

  def onStop() {

  }

  def onStart() {

  }
}