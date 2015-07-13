/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.http.ws

import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider
import play.api.http.Writeable
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import uk.gov.hmrc.play.http.writes.HttpWrites
import uk.gov.hmrc.play.http.{HttpPut, HttpResponse}
import MdcLoggingExecutionContext._

import scala.concurrent.Future

trait WSPut extends HttpPut with WSRequest {

  def doPut[I, O](url: String, body: I)(implicit rds: HttpWrites[I, O], hc: HeaderCarrier): Future[HttpResponse] = {
    buildRequest(url).put(body).map (new WSHttpResponse(_))
  }
}

