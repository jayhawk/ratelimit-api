package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.CSVExtractor
import play.api.libs.json._
import services.CSVData
import services.KeyStore
import services.APIKey
import org.joda.time.DateTime
import org.joda.time.Seconds

@Singleton
class APIController @Inject() extends Controller {

  private lazy val data = CSVExtractor.extract("public/hoteldb.csv")

  implicit val csvDataWrites = new Writes[CSVData] {
    def writes(csvData: CSVData) = Json.obj(
      "city" -> csvData.city,
      "hotelId" -> csvData.hotelId,
      "room" -> csvData.room,
      "price" -> csvData.price)
  }

  def findHotels(key: String, cityId: String, sort: String) = Action {
    val apiKey= KeyStore.getAPIKey(key)
    if (apiKey.canRequest) {
      val filteredData = data.filter(_.city == cityId).sortBy(_.price)
      val results = if (sort.equalsIgnoreCase("desc")) filteredData.reverse else filteredData
      KeyStore.addKey(apiKey.timeStamped)
      if (results.isEmpty) NotFound(s"No hotels with ID $cityId") else Ok(Json.toJson(results))
    } else Status(429)("Too many request")

  }

}
