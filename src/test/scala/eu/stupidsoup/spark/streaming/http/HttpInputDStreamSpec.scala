package eu.stupidsoup.spark.streaming.http

import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit

import fr.simply.util.ContentType
import fr.simply.{ StaticServerResponse, GET, StubServer }
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.http.HttpInputUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.specs2.Specification

import scala.concurrent.duration.FiniteDuration
import scala.io.Source

class HttpInputDStreamSpec extends Specification {
  def is = sequential ^
    s2"""
    HttpInputDStream
    ======================

      It should return a result from a local http server $basicTest
    """

  def basicTest = {
    startHttpServer

    val conf = new SparkConf().setMaster("local[2]").setAppName("HttpInputDStreamSpec")
    val ssc = new StreamingContext(conf, Seconds(1))
    val dstream: DStream[String] = HttpInputUtils.createStream(ssc, new URI("http://localhost:8888/test.json"), FiniteDuration(100, TimeUnit.MILLISECONDS), StorageLevel.MEMORY_AND_DISK_SER_2)
    dstream.saveAsTextFiles("/tmp/HttpInputDStreamSpec")
    ssc.start()
    ssc.awaitTermination(5050)
    ssc.stop()

    val result = new File("/tmp/").listFiles.toList.filter(_.getName.startsWith("HttpInputDStreamSpec")).flatMap {
      _.listFiles.toList.filter(_.getName.startsWith("part")).flatMap(Source.fromFile(_).getLines().map(_.trim).filter(!_.isEmpty).toList)
    }
    new File("/tmp/").listFiles.filter(_.getName.startsWith("HttpInputDStreamSpec")).foreach(FileUtils.deleteDirectory(_))
    (result.size must be greaterThan (30)) and
      (result.head must_== """{"test":"value"}""") and
      (result.last must_== """{"test":"value"}""")
  }

  def startHttpServer = {
    val route = GET(
      path = "/test.json",
      response = StaticServerResponse(ContentType("application/json"), """{"test":"value"}""", 200)
    )
    new StubServer(8888, route).start
  }
}
