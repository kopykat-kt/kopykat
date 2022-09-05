# Transformative for Kotlin

_When the author figures out how to upload the artifact to Maven, instructions on how to use the library will appear here._

One of the great features of Kotlin [data classes](https://kotlinlang.org/docs/data-classes.html) is their [`copy` method](https://kotlinlang.org/docs/data-classes.html#copying). But using it can become cumbersome very quickly, because you need to repeat the name of the field before and after.

```kotlin
data class Person(val name: String, val age: Int)

val p1 = Person("Alex", 1)
val p2 = p1.copy(age = p1.age + 1)  // too many 'age'!
```

This plug-in generates a new method `transform`, which instead of new *values*, takes as arguments the *transformation* that ought to be applied to each argument.

```kotlin
import fp.serrano.transformative

@transformative data class Person(val name: String, val age: Int)

val p1 = Person("Alex", 1)
val p2 = p1.transform(age = { it + 1 })
```

Note that you can easily use `transform` to simulate `copy`, simply by making the transformation return a constant value.

```kotlin
val p3 = p1.transform(age = { 10 })
```