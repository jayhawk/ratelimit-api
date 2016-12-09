package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller
import services.CSVData
import services.CSVExtractor
import services.KeyStore

@Singleton
class APIController @Inject() extends Controller {

  //CSV data
  private lazy val data = CSVExtractor.extract("public/hoteldb.csv")
  
  /*
   * JSON writes for CSVData for response
   */
  implicit val csvDataWrites = new Writes[CSVData] {
    def writes(csvData: CSVData) = Json.obj(
      "city" -> csvData.city,
      "hotelId" -> csvData.hotelId,
      "room" -> csvData.room,
      "price" -> csvData.price)
  }
 
  /**
   * Find hotels 
   * @param key API Key
   * @param cityId City ID e.g. Bangkok
   * @parma sort Sorting by default is ASC. Pass 'desc' for DESC sort.
   * 
   * 1. Get API key from KeyStore using key. If there is no key found in keystore, return 403.
   * 2. If there is an API key, check if it can make a request. If yes, pull out data according and return in JSON format with 200.
   * 3. If it cannot make a request, suspend the key and return 429.
   */
  def findHotels(key: String, cityId: String, sort: Option[String]) = Action {
    KeyStore.getAPIKey(key).fold(Status(403)("Invalid API key"))(apiKey =>
      if (apiKey.canRequest) {
        println("Requesting")
        val filteredData = data.filter(_.city == cityId) //Pull matched data with cityID
        val results = sort match {
          case Some(s) if s matches "(?i)asc" => filteredData.sortBy(_.price)
          case Some(s) if s matches "(?i)desc" => filteredData.sortBy(-_.price)
          case _ => filteredData
        }
        KeyStore.addKey(apiKey.timeStamped) //timestamp the key and add to keystore
        KeyStore.printKeys //debugging purpose
        if (results.isEmpty) NotFound(s"No hotels with ID $cityId") else Ok(Json.toJson(results))
      } else {
        println("Denied")
        KeyStore.addKey(apiKey.suspended) //suspend the key and add to keystore
        KeyStore.printKeys //debugging purpose
        Status(429)("Too many requests")
      })
  }
}
