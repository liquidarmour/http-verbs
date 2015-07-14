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

package uk.gov.hmrc.play.http.streaming

import java.nio.ByteBuffer

import com.ning.http.client.providers.netty.FeedableBodyGenerator
import com.ning.http.client.{Response => AHCResponse, _}
import play.api.Play.current
import play.api.http.HttpVerbs.{PUT => PUT_VERB}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.ws.ning.NingWSResponse
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.hmrc.play.audit.http.{HeaderCarrier, HttpAuditing}
import uk.gov.hmrc.play.http.logging.ConnectionTracing
import uk.gov.hmrc.play.http.reads.HttpReads
import uk.gov.hmrc.play.http.writes.HttpWrites
import uk.gov.hmrc.play.http.{HttpResponse, HttpVerb}

import scala.concurrent.{ExecutionContext, Future, Promise}

trait StreamingPUT extends HttpVerb with ConnectionTracing with HttpAuditing {

  private lazy val client: AsyncHttpClient = WS.client.underlying

  protected def doPut[A](url: String, body: Enumerator[Array[Byte]])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val (request, generator) = buildRequest(url)
    execute(request, generator)
  }

  def PUT[I, O](url: String, body: I)(implicit wts: HttpWrites[I, Enumerator[Array[Byte]]], rds: HttpReads[O], hc: HeaderCarrier, ec: ExecutionContext): Future[O] = {
    withTracing(PUT_VERB, url) {
      val httpResponse = doPut(url, wts.writes(body))
      auditRequestWithResponseF(url, PUT_VERB, wts.audit(body), httpResponse)
      mapErrors(PUT_VERB, url, httpResponse).map(response => rds.read(PUT_VERB, url, response))
    }
  }

  def execute(request: com.ning.http.client.Request, bodyGenerator: FeedableBodyGenerator)(implicit ec: ExecutionContext): Future[HttpResponse] = {
    // We execute the request, but we can send body chunks afterwards.
//    val bodyGenerator = new FeedableBodyGenerator

    val result = Promise[WSResponse]()
    client.executeRequest(request, new AsyncCompletionHandler[AHCResponse]() {

      override def onCompleted(response: AHCResponse) = {
        result.success(NingWSResponse(response))
        response
      }

      override def onThrowable(t: Throwable) = {
        result.failure(t)
      }
    })

    val resultFuture = Iteratee.fold[Array[Byte], FeedableBodyGenerator](bodyGenerator) {
      (generator, bytes) =>
        val isLast = false
        generator.feed(ByteBuffer.wrap(bytes), isLast)
        generator
    } mapM { generator =>
      val isLast = true
      generator.feed(ByteBuffer.wrap(Array[Byte]()), isLast)
      result.future
    }

    resultFuture.run.map { response =>
      HttpResponse(responseStatus = response.status, responseJson = Option(response.json), responseHeaders = response.allHeaders, responseString = Option(response.body))
    }
  }

  def buildRequest(url: String, contentType: String = "binary/octet-stream"): (Request, FeedableBodyGenerator) = {
    val bodyGenerator = new FeedableBodyGenerator()
    val request = new RequestBuilder("PUT")
      .setUrl(url)
      .setHeader("Content-Type", contentType)
      .setBody(bodyGenerator)
      .build()
    (request, bodyGenerator)
  }
}