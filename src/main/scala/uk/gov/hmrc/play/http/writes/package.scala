package uk.gov.hmrc.play.http

package object writes {

  implicit val enumeratorHttpWrites = new EnumeratorHttpWrites()
}
