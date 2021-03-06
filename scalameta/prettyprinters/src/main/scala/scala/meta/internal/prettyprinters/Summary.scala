package scala.meta
package internal
package prettyprinters

import org.scalameta.show._
import Show.{ sequence => s, repeat => r, indent => i, newline => n }
import scala.compat.Platform.EOL
import scala.annotation.implicitNotFound
import scala.meta.prettyprinters.Syntax

@implicitNotFound(msg = "don't know how to show[Summary] for ${T}")
private[meta] trait Summary[T] extends Show[T]
private[meta] object Summary {
  def apply[T](f: T => Show.Result): Summary[T] = new Summary[T] { def apply(input: T) = f(input) }

  implicit def summary[T: Syntax]: Summary[T] = Summary { x =>
    var result = x.show[Syntax].replace(EOL, " ")
    if (result.length > 60) result = result.take(60) + "..."
    s(result)
  }
}
