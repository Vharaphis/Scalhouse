// Basics Worksheet

// Expressions: more complex expressions expressions can be created by combination using operators
// https://scala-lang.org/api/3.x/scala/Int.html
// The compiler statically checks that you don’t combine incompatible expressions (types)
// This is called static typing

// `val` evaluates when defined
// `def` evaluates on every call

// Adding the 's' before the string formats allows formatting (replaces variables by their content)
val area = (4 * 6) / 2
s"area of triangle is equal to $area"

// val = Immutable and var = mutable
val z = 41 // z = 42 doesn't work
var z2 = 41 // z2 = 42 does work


// Immutable collections
val xs = List(1, 2, 3) //Immutable
import scala.collection.mutable.ListBuffer
val ys = ListBuffer(1, 2) //Mutable


// Functions: defining a function. Unit == void in Java. Possible to give a default value.
def append(x: Int, y: List[Int] = List(1,2,3)): List[Int] = y :+ x
append(x=5, y=List(1,2,3)) // We can also explicitly name the parameters.


// High-order function: function that takes function as parameter
def squareOf(x: Int): Int = x * x
def sumOf(x: Int, y: Int, f: Int => Int): Int = f(x) + f(y)

// Assigning a function to a val & using
val identityVal: Int => Int = x => x
sumOf(2, 3, identityVal)
sumOf(2, 3, x => x)
sumOf(1, 2, squareOf)



// A function with no parameter that returns a function Int => Int
// Creates a new function every time the function is called
// The type if squareDef is Function0[Function1[Int, Int]]
def squareDef(): Int => Int = { //Evaluated EVERY TIME
  print(">>>lazy val<<<")
  x => x * x
}
lazy val squareLazyVal: Int => Int = { //Evaluated ONCE
  print(">>>lazy val<<<")
  x => x * x
}
sumOf(2, 3, squareDef())
sumOf(2, 3, squareDef())
sumOf(2, 3, squareLazyVal)
sumOf(2, 3, squareLazyVal)


// Call-by-value and call-by-name
// Call-by-value and call-by-name are two evaluation strategies
// Both strategies reduce to the same final values as long as
// - the reduced expression consists of pure functions, and
// - both evaluations terminate.
// Call-by-value evaluates every function argument only once, even if not used in the function body.
// Call-by-name does not evaluate if the corresponding parameter is unused in the evaluation of the function body.
// Scala normally uses call-by-value.

def time(): Long = {
  println("Executing time()")
  System.nanoTime
}

// `t` is now defined as a by-value parameter
// `time()` is called once
def execByValue(t: Long): Long = {
  println("Entered exec, calling t...")
  println(s"t is $t")
  println("Calling t again...")
  t
}

execByValue(time())

// `t` is now defined as a by-name parameter
// `time()` is called twice
def execByName(t: => Long) = {
  println("Entered exec, calling t...")
  println(s"t is $t")
  println("Calling t again...")
  t
}

execByName(time())

// `t` is now defined as a by-value parameter
// `time()` is called once
def execConstantByValue(t: Long): Long = 35221213909789L
execConstantByValue(time())

// `t` is now defined as a by-name parameter
// `time()` is never called
def execConstantByName(t: => Long) = 35221213909789L
execConstantByName(time())