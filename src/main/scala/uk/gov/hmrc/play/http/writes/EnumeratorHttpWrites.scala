package uk.gov.hmrc.play.http.writes

import play.api.libs.iteratee.Enumerator

class EnumeratorHttpWrites extends HttpWrites[Enumerator[Array[Byte]], Enumerator[Array[Byte]]] {

  override def writes(requestContent: Enumerator[Array[Byte]]): Enumerator[Array[Byte]] = requestContent

  override def audit(content: Enumerator[Array[Byte]]): Option[_] = None

}
