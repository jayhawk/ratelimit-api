package services

import java.util.concurrent.TimeUnit

import scala.Boolean
import scala.collection.concurrent.TrieMap
import scala.io.Source

import org.joda.time.DateTime
import org.joda.time.Seconds

import com.typesafe.config.ConfigException

import play.api.Play

/**
 * Play configuration object which reads API global rate and suspended time from config file.
 */
object Configuration {
  private val underlyingConfig = Play.current.configuration.underlying
  val globalRate = try {
    underlyingConfig.getDuration("api.global.rate", TimeUnit.SECONDS).toInt //Getting time in seconds
  } catch {
    case cee: ConfigException.Missing => 10 //If config is missing, use 10 seconds
    case e: Exception => throw e
  }
  val suspendedTime = try {
    underlyingConfig.getDuration("api.suspendedTime", TimeUnit.SECONDS).toInt //Getting time in seconds
  } catch {
    case cee: ConfigException.Missing => 5 * 60 //If config is missing, use 5 minutes
    case e: Exception => throw e
  }
}

/**
 * Keystore with TrieMap to store API keys.
 */
object KeyStore {
  private var values: TrieMap[String, APIKey] = TrieMap("apikey1" -> APIKey("apikey1", rate = 10), "apikey2" -> APIKey("apikey2"))
  def getAPIKey(key: String): Option[APIKey] = values.get(key)
  def addKey(apiKey: APIKey) = values.put(apiKey.key, apiKey)
  def printKeys = values foreach println
}

/**
 * API Key class
 * @param key API key
 * @param lastAccessOpt API key last access time
 * @param suspendedTimeOpt Time when API key got suspended
 * @param rate individual rate limit of API key. Default value is global rate limit
 */

case class APIKey(key: String, lastAccessOpt: Option[DateTime] = None, suspendedTimeOpt: Option[DateTime] = None, rate: Int = Configuration.globalRate) {

  /**
   * To check if API key is suspended by checking @param suspendedTimeOpt
   */
  def isSuspended: Boolean = suspendedTimeOpt.fold(false) { time =>
    val timeTakenAfterSuspension = Seconds.secondsBetween(time, DateTime.now())
    val globalSuspendedTime = Seconds.seconds(Configuration.suspendedTime)
    println(s"Time taken after suspensin $timeTakenAfterSuspension")
    timeTakenAfterSuspension.isLessThan(globalSuspendedTime)
  }

  /**
   * To check if API key can make a request by checking @param lastAccessOpt and isSuspended
   */
  def canRequest: Boolean = {
    !isSuspended &&
      lastAccessOpt.fold(true)(lastAccess =>
        Seconds.secondsBetween(lastAccess, DateTime.now()).isGreaterThan(Seconds.seconds(rate)))
  }
  /**
   * To timestamp APIkey. This replace existing @param lastAccessOpt with current date time
   */
  def timeStamped: APIKey = APIKey(key, lastAccessOpt = Option(DateTime.now()), rate = this.rate)

  /**
   * To check if API key has already suspended.
   */
  lazy val hasAlreadySuspended: Boolean = suspendedTimeOpt.isDefined

  /**
   * To suspend APIKey.
   */
  def suspended: APIKey = if (hasAlreadySuspended) this else APIKey(key, suspendedTimeOpt = Option(DateTime.now()), rate = this.rate)
}

/**
 * DTO of CSV data
 */
case class CSVData(city: String, hotelId: Double, room: String, price: Double)

/**
 * Helper to extract data from CSV file
 */
object CSVExtractor {
  def extract(filePath: String): Seq[CSVData] = {
    Source.fromFile(filePath).getLines().drop(1).map(_.split(",") match {
      case Array(city, hotelId, room, price) => Option(CSVData(city, hotelId.toDouble, room, price.toDouble))
      case _ => None
    }).toSeq.flatten
  }
}