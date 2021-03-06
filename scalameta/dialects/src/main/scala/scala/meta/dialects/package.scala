package scala.meta

import scala.annotation.implicitNotFound
import org.scalameta.adt._
import org.scalameta.dialects._
import scala.meta.dialects._
import scala.language.experimental.macros

// NOTE: can't put Dialect into scala.meta.Dialects
// because then implicit scope for Dialect lookups will contain members of the package object
// i.e. both Scala211 and Dotty, which is definitely not what we want
@root trait Dialect extends Serializable {
  // Canonical name for the dialect.
  // Can be used to uniquely identify the dialect, e.g. during serialization/deserialization.
  def name: String

  // The sequence of characters that's used to express a bind
  // to a sequence wildcard pattern.
  def bindToSeqWildcardDesignator: String

  // Are XML literals supported by this dialect?
  // We plan to deprecate XML literal syntax, and some dialects
  // might go ahead and drop support completely.
  def allowXmlLiterals: Boolean

  // Permission to tokenize repeated dots as ellipses.
  // Necessary to support quasiquotes, e.g. `q"foo(..$args)"`.
  def allowEllipses: Boolean

  // Are type lambdas supported in this dialect?
  // At the moment, this is exclusive for quasiquotes.
  // For more info, see https://github.com/scalameta/scalameta/commit/e2317e8655ead8a2a391355ed91bccf98eadb2c7
  def allowTypeLambdas: Boolean

  // Are method types supported in this dialect?
  // Only supported in quasiquotes, and even that's too much.
  // The original idea of scala.meta was to enable metaprogramming with existing syntax,
  // but supporting `Term.tpe` for all terms, including method references, presents a difficult situation.
  // After some deliberation, I decided to give up the aforementioned idea and make quasiquotes support
  // more than just standard syntax of Scala.
  def allowMethodTypes: Boolean
}

package object dialects {
  @leaf implicit object Scala211 extends Dialect {
    def name = "Scala211"
    def bindToSeqWildcardDesignator = "@" // List(1, 2, 3) match { case List(xs @ _*) => ... }
    def allowXmlLiterals = true // Not even deprecated yet, so we need to support xml literals
    def allowEllipses = false // Vanilla Scala doesn't support ellipses, somewhat similar concept is varargs and _*
    def allowTypeLambdas = false // Vanilla Scala doesn't support type lambdas
    def allowMethodTypes = false // Vanilla Scala doesn't support method types
    private def writeReplace(): AnyRef = new Dialect.SerializationProxy(this)
  }

  @leaf implicit object Dotty extends Dialect {
    def name = "Dotty"
    def bindToSeqWildcardDesignator = ":" // // List(1, 2, 3) match { case List(xs: _*) => ... }
    def allowXmlLiterals = false // Dotty parser doesn't have the corresponding code, so it can't really support xml literals
    def allowEllipses = false // Vanilla Dotty doesn't support ellipses, somewhat similar concept is varargs and _*
    def allowTypeLambdas = false // Vanilla Dotty doesn't support type lambdas
    def allowMethodTypes = false // Vanilla Dotty doesn't support method types
    private def writeReplace(): AnyRef = new Dialect.SerializationProxy(this)
  }

  @leaf class Quasiquote(dialect: Dialect) extends Dialect {
    def name = s"Quasiquote(${dialect.name})"
    def bindToSeqWildcardDesignator = dialect.bindToSeqWildcardDesignator
    def allowXmlLiterals = dialect.allowXmlLiterals
    def allowEllipses = true
    def allowTypeLambdas = true
    def allowMethodTypes = true
    private def writeReplace(): AnyRef = new Dialect.SerializationProxy(this)
  }
}

object Dialect {
  // NOTE: See https://github.com/scalameta/scalameta/issues/253 for discussion.
  implicit def currentDialect: Dialect = macro CurrentDialect.impl

  private val QuasiquoteRx = "^Quasiquote\\((.*?)\\)$".r
  def forName(name: String): Dialect = name match {
    case "Scala211" => scala.meta.dialects.Scala211
    case "Dotty" => scala.meta.dialects.Dotty
    case QuasiquoteRx(name) => Quasiquote(Dialect.forName(name))
    case _ => throw new DialectException(name, s"unknown dialect $name")
  }

  @SerialVersionUID(1L) private[meta] class SerializationProxy(@transient private var orig: Dialect) extends Serializable {
    private def writeObject(out: java.io.ObjectOutputStream): Unit = {
      out.writeObject(orig.name)
    }
    private def readObject(in: java.io.ObjectInputStream): Unit = {
      val name = in.readObject.asInstanceOf[String]
      orig = Dialect.forName(name)
    }
    private def readResolve(): AnyRef = orig
    override def toString = s"Proxy($orig)"
  }
}
