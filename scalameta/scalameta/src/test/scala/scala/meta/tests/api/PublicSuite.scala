// package scala.meta.tests
// package api

import org.scalatest._
import org.scalameta.tests._

class PublicSuite extends FunSuite {
  test("interactive APIs without import") {
    assert(typecheckError("""
      implicit val c: scala.meta.interactive.Context = ???
      c.load(??? : scala.meta.artifacts.Artifact)
    """) === "method load in trait Context cannot be accessed in scala.meta.interactive.Context")
  }

  test("interactive APIs when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.interactive.Context = ???
      c.load(??? : scala.meta.artifacts.Artifact)
    """) === "")
  }

  test("interactive context APIs") {
    assert(typecheckError("""
      (??? : scala.meta.interactive.Context).load(??? : scala.meta.artifacts.Artifact)
    """) === "method load in trait Context cannot be accessed in scala.meta.interactive.Context")
  }

  // TODO: wat is this?!
  test("artifact APIs without import") {
    assert(typecheckError("""
      val domain: scala.meta.artifacts.Domain = ???
      domain.sources
    """) === """
      |type mismatch;
      | found   : domain.type (with underlying type scala.meta.artifacts.Domain)
      | required: ?{def sources: ?}
      |Note that implicit conversions are not applicable because they are ambiguous:
      | both method XtensionDomain in trait Api of type (domain: scala.meta.artifacts.Domain)Api.this.XtensionDomain
      | and method XtensionDomain in trait Api of type (domain: scala.meta.artifacts.Domain)Api.this.XtensionDomain
      | are possible conversion functions from domain.type to ?{def sources: ?}
    """.trim.stripMargin)
  }

  test("artifact APIs without resolver") {
    assert(typecheckError("""
      import scala.meta._
      val domain: scala.meta.artifacts.Domain = ???
      domain.sources
    """) === "")
  }

  test("artifact APIs with resolver") {
    assert(typecheckError("""
      import scala.meta._
      implicit val r: scala.meta.artifacts.Resolver = ???
      val domain: scala.meta.artifacts.Domain = ???
      domain.sources
    """) === "")
  }

  test("quasiquotes without import") {
    assert(typecheckError("""
      q"hello"
    """) === "value q is not a member of StringContext")
  }

  test("quasiquotes without static dialect") {
    assert(typecheckError("""
      import scala.meta._
      implicit val dialect: scala.meta.Dialect = ???
      q"hello"
    """) === "dialect does not have precise enough type to be used in quasiquotes (to fix this, import something from scala.dialects, e.g. scala.meta.dialects.Scala211)")
  }

  test("quasiquotes when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      q"hello"
    """) === "")
  }

  test("semantic APIs without import") {
    assert(typecheckError("""
      (??? : scala.meta.Ref).defn
    """) === "this method requires an implicit scala.meta.semantic.Context")
  }

  test("semantic APIs without context") {
    assert(typecheckError("""
      import scala.meta._
      (??? : Ref).defn
    """) === "this method requires an implicit scala.meta.semantic.Context")
  }

  test("semantic APIs when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.semantic.Context = ???
      (??? : Ref).defn
    """) === "")
  }

  test("=:= without import") {
    assert(typecheckError("""
      (??? : scala.meta.Tree) =:= (??? : scala.meta.Tree)
    """) === "this method requires an implicit scala.meta.semantic.Context")
  }

  test("=:= without context") {
    assert(typecheckError("""
      import scala.meta._
      (??? : Tree) =:= (??? : Tree)
    """) === "this method requires an implicit scala.meta.semantic.Context")
  }

  test("=:= when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.semantic.Context = ???
      (??? : Tree) =:= (??? : Tree)
    """) === "")
  }

  test("=:= with insufficient precision - 1") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.semantic.Context = ???
      (??? : Scope) =:= (??? : Tree)
    """) === "can't compare scala.meta.Scope and scala.meta.Tree")
  }

  test("=:=with insufficient precision - 2") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.semantic.Context = ???
      (??? : Stat) =:= (??? : Term)
    """) === "can't compare scala.meta.Stat and scala.meta.Term")
  }

  test("=:= with unrelated nodes") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.semantic.Context = ???
      (??? : Ref) =:= (??? : Member)
    """) === "can't compare scala.meta.Ref and scala.meta.Member")
  }

  test("Tree.desugar") {
    assert(typecheckError("""
      import scala.meta._
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Tree).desugar
    """) === "value desugar is not a member of scala.meta.Tree")
  }

  test("Term.desugar") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      ((??? : api.Term).desugar): api.Term
      ((??? : impl.Term).desugar): api.Term
    """) === "")
  }

  test("Tree.tpe") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Tree).tpe
    """) === "value tpe is not a member of scala.meta.Tree")
  }

  test("Type.tpe") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      t"List".tpe: api.Type
      t"List[Int]".tpe: api.Type
    """) === "value tpe is not a member of scala.meta.Type")
  }

  test("Term.tpe") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      ((??? : api.Term).tpe): api.Type
      ((??? : impl.Term).tpe): api.Type
      q"x".tpe: api.Type
    """) === "")
  }

  test("Member.tpe") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      ((??? : api.Member).tpe): api.Type
      ((??? : impl.Member).tpe): api.Type
      q"class C".tpe: api.Type
    """) === "")
  }

  test("Tree.defn") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Tree).defn
    """) === "value defn is not a member of scala.meta.Tree")
  }

  test("Type.Apply.defn") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      t"List[Int]".defn
    """) === "value defn is not a member of scala.meta.Type")
  }

  test("Ref.defn") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Ref).defn: api.Member
      (??? : api.Term.Ref).defn: api.Member.Term
      (??? : api.Type.Ref).defn: api.Member
      (??? : impl.Ref).defn: api.Member
      (??? : impl.Term.Ref).defn: api.Member.Term
      (??? : impl.Type.Ref).defn: api.Member
      q"x".defns: scala.collection.immutable.Seq[api.Member.Term]
      q"x".defn: api.Member.Term
      t"x".defns: scala.collection.immutable.Seq[api.Member]
      t"x".defn: api.Member
      q"class C".name: api.Type.Name
      q"class C".name.defn: api.Member
      q"class C".source.name.defn: api.Member
      q"object M".name: api.Term.Name
      q"object M".name.defn: api.Member.Term
      q"object M".source.name.defn: api.Member.Term
    """) === "")
  }

  test("Tree.members") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Tree).members
    """) === "value members is not a member of scala.meta.Tree")
  }

  test("Ref.members") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      q"scala".members: scala.collection.immutable.Seq[api.Member]
      q"(1 + 2).toString".members: scala.collection.immutable.Seq[api.Member]
    """) === "")
  }

  test("Type.members") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Type).members: scala.collection.immutable.Seq[api.Member]
      (??? : impl.Type).members: scala.collection.immutable.Seq[api.Member]
      t"List".members: scala.collection.immutable.Seq[api.Member]
      q"List".tpe.members: scala.collection.immutable.Seq[api.Member]
    """) === "")
  }

  test("Member.members") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      implicit val c: scala.meta.semantic.Context = ???
      import scala.{meta => api}
      import scala.meta.internal.{ast => impl}
      (??? : api.Member).members: scala.collection.immutable.Seq[api.Member]
      (??? : impl.Member).members: scala.collection.immutable.Seq[api.Member]
      (??? : api.Member).members(q"foo"): api.Member
      (??? : api.Member).members(t"foo"): api.Member
      t"List".defs("head"): api.Member.Term
      t"List".defs("head").paramss: scala.collection.immutable.Seq[scala.collection.immutable.Seq[api.Member]]
      t"List".defn.tparams: scala.collection.immutable.Seq[api.Member]
    """) === "")
  }

  // TODO: this error is somewhat confusing
  test("internal helpers of semantic APIs") {
    assert(typecheckError("""
      import scala.meta._
      (??? : Member).internalAll(???)
    """) === "method internalAll in trait XtensionSemanticScopeLike cannot be accessed in meta.XtensionSemanticScope")
  }

  // TODO: this error is somewhat confusing
  test("semantic context APIs (opaque)") {
    assert(typecheckError("""
      (??? : scala.meta.semantic.Context).isSubtype(???, ???)
    """) === "method isSubtype in trait Context cannot be accessed in scala.meta.semantic.Context")
  }

  test("semantic context APIs (the only transparent one)") {
    assert(typecheckError("""
      (??? : scala.meta.semantic.Context).dialect
    """) === "")
  }

  test("parse without import") {
    assert(typecheckError("""
      "".parse[scala.meta.Term]
    """) === "value parse is not a member of String")
  }

  test("parse without input-likeness") {
    assert(typecheckError("""
      import scala.meta._
      1.parse[Term]
    """) === "don't know how to convert Int to scala.meta.inputs.Input")
  }

  test("parse without parseability") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      "".parse[Int]
    """) === "don't know how to parse into Int")
  }

  test("parse when everything's correct (static dialect)") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      "".parse[Term]
    """) === "")
  }

  test("parse when everything's correct (dynamic dialect)") {
    assert(typecheckError("""
      import scala.meta._
      implicit val dialect: scala.meta.Dialect = ???
      "".parse[Term]
    """) === "")
  }

  test("parse with various input types") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      (??? : Input).parse[Term]
      (??? : String).parse[Term]
      (??? : java.io.File).parse[Term]
      (??? : Tokens).parse[Term]
      (??? : Array[Char]).parse[Term]
    """) === "")
  }

  test("tokens without import") {
    assert(typecheckError("""
      "".tokens
    """) === "value tokens is not a member of String")
  }

  test("tokens without input-likeness") {
    assert(typecheckError("""
      import scala.meta._
      1.tokens
    """) === "don't know how to convert Int to scala.meta.inputs.Content")
  }

  test("tokens when everything's correct (static dialect)") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      "".tokens
    """) === "")
  }

  test("tokens when everything's correct (dynamic dialect)") {
    assert(typecheckError("""
      import scala.meta._
      implicit val dialect: scala.meta.Dialect = ???
      "".tokens
    """) === "")
  }

  test("tokens with various input types") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      (??? : Input).tokens
      (??? : String).tokens
      (??? : java.io.File).tokens
      (??? : Tokens).tokens
      (??? : Array[Char]).tokens
    """) === "")
  }

  test("show[Code] without import") {
    assert(typecheckError("""
      (??? : scala.meta.Tree).show[Code]
    """) === "not found: type Code")
  }

  test("show[Code] when everything's correct (static dialect)") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      (??? : Tree).show[Code]
    """) === "")
  }

  test("show[Code] when everything's correct (dynamic dialect)") {
    assert(typecheckError("""
      import scala.meta._
      implicit val dialect: scala.meta.Dialect = ???
      (??? : Tree).show[Code]
    """) === "")
  }

  test("show[Syntax] without import") {
    assert(typecheckError("""
      (??? : scala.meta.Tree).show[Syntax]
    """) === "not found: type Syntax")
  }

  test("show[Syntax] when everything's correct (static dialect)") {
    assert(typecheckError("""
      import scala.meta._
      import scala.meta.dialects.Scala211
      (??? : Tree).show[Syntax]
    """) === "")
  }

  test("show[Syntax] when everything's correct (dynamic dialect)") {
    assert(typecheckError("""
      import scala.meta._
      implicit val dialect: scala.meta.Dialect = ???
      (??? : Tree).show[Syntax]
    """) === "")
  }

  test("show[Raw] without import") {
    assert(typecheckError("""
      (??? : scala.meta.Tree).show[Raw]
    """) === "not found: type Raw")
  }

  test("show[Raw] when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      (??? : Tree).show[Raw]
    """) === "")
  }

  test("show[Structure] without import") {
    assert(typecheckError("""
      (??? : scala.meta.Tree).show[Structure]
    """) === "not found: type Structure")
  }

  test("show[Structure] when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      (??? : Tree).show[Structure]
    """) === "")
  }

  test("show[Semantics] without import") {
    assert(typecheckError("""
      (??? : scala.meta.Tree).show[Semantics]
    """) === "not found: type Semantics")
  }

  test("show[Semantics] without context") {
    assert(typecheckError("""
      import scala.meta._
      (??? : Tree).show[Semantics]
    """) === "don't know how to show[Semantics] for scala.meta.Tree (be sure to have an implicit scala.meta.semantic.Context in scope)")
  }

  test("show[Semantics] when everything's correct") {
    assert(typecheckError("""
      import scala.meta._
      implicit val c: scala.meta.semantic.Context = ???
      (??? : Tree).show[Semantics]
    """) === "")
  }
}
