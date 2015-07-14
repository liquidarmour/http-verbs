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

trait HttpWrites[I, B] {

  def writes(requestContent: I): B

  def audit(content: I): Option[_]

}

object HttpWrites {

  def apply[I, B](writef: (I) => B): HttpWrites[I, B] = new HttpWrites[I, B] {

    def writes(content: I) = writef(content)

    override def audit(content: I): Option[_] = None
  }
}