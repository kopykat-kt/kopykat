# Transformative for Kotlin

_When the author figures out how to upload the artifact to Maven, instructions on how to use the library will appear here._

One of the great features of Kotlin [data classes](https://kotlinlang.org/docs/data-classes.html) is their [`copy` method](https://kotlinlang.org/docs/data-classes.html#copying). But using it can become cumbersome very quickly, because you need to repeat the name of the field before and after.

```kotlin
data class Person(val name: String, val age: Int)

val p1 = Person("Alex", 1)
val p2 = p1.copy(age = p1.age + 1)  // too many 'age'!
```

This plug-in generates a new method `transform`, which instead of new *values*, takes as arguments the *transformations* that ought to be applied to each argument.

```kotlin
import fp.serrano.transformative

@transformative data class Person(val name: String, val age: Int)

val p1 = Person("Alex", 1)
val p2 = p1.transform(age = { it + 1 })
```

Note that you can use `transform` to simulate `copy`, by making the transformation return a constant value.

```kotlin
val p3 = p1.transform(age = { 10 })
```

## List transformations

If a field has type `List<T>`, then an additional argument is added to the `transform` function with the name `${field}Each`. The block given to that argument is applied to each element of the list. This is like an implicit `map`.

```kotlin
@transformative data class Person(val name: String, val age: Int, val nicknames: List<String>)

val p1 = Person("Alex", 1, listOf("Serras"))
val p2 = p1.transform(nicknamesEach = { it.lowercase() })
```