package services

import scala.io.Source
import org.joda.time.DateTime
import scala.collection.concurrent.TrieMap
import org.joda.time.Seconds

object KeyStore {
  private var values: TrieMap[String, APIKey] = TrieMap.empty
  def getAPIKey(key: String): APIKey = values.getOrElse(key, APIKey(key))
  def addKey(apiKey: APIKey) = values.put(apiKey.key, apiKey)
}

case class APIKey(key: String, lastAccessOpt: Option[DateTime] = None, suspendedTimeOpt: Option[DateTime] = None) {
  lazy val isSuspended: Boolean = suspendedTimeOpt.isDefined
  def canRequest: Boolean = {
    lastAccessOpt.fold(true)(lastAccess =>
      !isSuspended && Seconds.secondsBetween(lastAccess, DateTime.now()).isGreaterThan(Seconds.seconds(10)))
  }
  def timeStamped: APIKey = APIKey(key, Option(DateTime.now()))
}

case class CSVData(city: String, hotelId: Double, room: String, price: Double)

object CSVExtractor {
  def extract(filePath: String): Seq[CSVData] = {
    Source.fromFile(filePath).getLines().drop(1).map(_.split(",") match {
      case Array(city, hotelId, room, price) => Option(CSVData(city, hotelId.toDouble, room, price.toDouble))
      case _ => None
    }).toSeq.flatten
  }
}