import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import services.CSVExtractor
import org.joda.time.Seconds
import org.joda.time.DateTime
import services.CSVData

class CSVTest extends PlaySpec with OneAppPerTest {

  "CSV extractor" should {
    "extracts data" in {
      val data = CSVExtractor.extract("public/hoteldb.csv")
      data must have size (26)
      data.head must equal(CSVData("Bangkok", 1, "Deluxe", 1000))
    }
  }
  
  "API key" should {
    "returns false" in {
      println(Seconds.secondsBetween(DateTime.now(), DateTime.now().plusSeconds(10)).isGreaterThan(Seconds.seconds(10)))
    }
  }
}