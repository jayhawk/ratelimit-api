import org.scalatestplus.play.OneAppPerTest
import org.scalatestplus.play.PlaySpec

import play.api.test.FakeRequest
import play.api.test.Helpers.FORBIDDEN
import play.api.test.Helpers.GET
import play.api.test.Helpers.NOT_FOUND
import play.api.test.Helpers.OK
import play.api.test.Helpers.TOO_MANY_REQUESTS
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import services.CSVData
import services.CSVExtractor

class APITest extends PlaySpec with OneAppPerTest {

  lazy val data = CSVExtractor.extract("public/hoteldb.csv")

  "CSV extractor" should {
    "extracts data" in {
      data must have size (26)
      data.head must equal(CSVData("Bangkok", 1, "Deluxe", 1000))
    }
  }

  "Find Hotels action" should {
    "returns 200 with valid API key" in {
      route(app, FakeRequest(GET, "/api/hotels?key=apikey1&cityId=Bangkok&sort=desc")).map(status(_)) mustBe Some(OK)
    }
    "returns 429 with multiple requests" in {
      route(app, FakeRequest(GET, "/api/hotels?key=apikey1&cityId=Bangkod&sort=desc")).map(status(_)) mustBe Some(TOO_MANY_REQUESTS)
    }
    "returns 200 after 20 seconds suspension" in {
      Thread.sleep(1000 * 5)
      //Checking after 5 seconds
      route(app, FakeRequest(GET, "/api/hotels?key=apikey1&cityId=Bangkok&sort=desc")).map(status(_)) mustBe Some(TOO_MANY_REQUESTS)
      Thread.sleep(1000 * 15)
      //Checking after 20 seconds
      route(app, FakeRequest(GET, "/api/hotels?key=apikey1&cityId=Bangkok&sort=desc")).map(status(_)) mustBe Some(OK)
    }
    "returns 403 with invalid API key" in {
      route(app, FakeRequest(GET, "/api/hotels?key=apikey3&cityId=Bangkok&sort=desc")).map(status(_)) mustBe Some(FORBIDDEN)
    }
    "returns 404 with wrong city ID" in {
      route(app, FakeRequest(GET, "/api/hotels?key=apikey2&cityId=Bangkod&sort=desc")).map(status(_)) mustBe Some(NOT_FOUND)
    }
  }
}