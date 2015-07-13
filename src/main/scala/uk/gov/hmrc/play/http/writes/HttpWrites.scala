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

package uk.gov.hmrc.play.http.writes

import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.http.{HttpRequest, HttpResponse}

trait HttpWrites[I, O] {

  def write(requestContent: I): O

}

object HttpWrites {

  def apply[I, O](writef: (String, String, HttpRequest)(implicit headerCarrier: HeaderCarrier) => HttpRequest[]): HttpWrites[I, O] = new HttpWrites[I, O] {
    def write(method: String, url: String, response: HttpResponse) = writef(method, url, response)
  }

  def always[O](const: O): HttpWrites[O] = HttpWrites((_,_,_) => const)
}