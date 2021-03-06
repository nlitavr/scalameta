package scala.meta.tests
package scalameta
package prettyprinters

import org.scalatest._
import scala.reflect.runtime.universe._
import scala.meta.prettyprinters._
import scala.meta.internal.ast._
import scala.meta.dialects.Scala211

class QuasiSuite extends FunSuite {
  val XtensionQuasiquoteTerm = "shadow scala.meta quasiquotes"
  test("$x") {
    assert(Term.Quasi(0, q"x").show[Syntax] === "${x @ Term}")
  }
  test("..$xs") {
    assert(Term.Quasi(1, Term.Quasi(0, q"xs")).show[Syntax] === "..${xs @ Term}")
  }
}