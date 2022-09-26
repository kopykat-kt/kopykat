<!-- TOC -->

* [What can KopyKat do?](#what-can-kopykat-do)
    * [Mutable `copy`](#mutable-copy)
        * [Nested mutation](#nested-mutation)
    * [Mapping `copyMap`](#mapping-copymap)
    * [`copy` for sealed hierarchies](#copy-for-sealed-hierarchies)
* [Using KopyKat in your project](#using-kopykat-in-your-project)
    * [Customizing the generation](#customizing-the-generation)
* [What about optics?](#what-about-optics)
<!-- TOC -->

One of the great features of Kotlin [data classes](https://kotlinlang.org/docs/data-classes.html) is
their [`copy` method](https://kotlinlang.org/docs/data-classes.html#copying). But using it can become cumbersome very
quickly, because you need to repeat the name of the field before and after.

```kotlin
data class Person(val name: String, val age: Int)

val p1 = Person("Alex", 1)
val p2 = p1.copy(age = p1.age + 1)  // too many 'age'!
```

## What can KopyKat do?

This plug-in generates a couple of new methods that make working with immutable (read-only) types, like data classes and
value classes, more convenient.

![IntelliJ showing the methods](https://github.com/kopykat-kt/kopykat/blob/main/intellij.png?raw=true)

### Mutable `copy`

This new version of `copy` takes a *block* as a parameter. Within that block, mutability is simulated; the final
assignment of each (mutable) variable becomes the value of the new copy. These are generated for both data classes and
value classes.

```kotlin
val p1 = Person("Alex", 1)
val p2 = p1.copy { 
  age++
}
```

You can use `old` to access the previous (immutable) value, before any changes.

```kotlin
val p3 = p1.copy { 
  age++
  if (notTheirBirthday) {
    age = old.age  // get the previous value
  }
}
```

#### Nested mutation

If you have a data class that contains another data class (or value class) as a property, you can also make changes to
inner types. Let's say we have these types:

```kotlin
data class Person(val name: String, val job: Job)
data class Job(val title: String)

val p1 = Person(name = "John", job = Job("Developer"))
```

Currently, to do mutate inner types you have to do the following:

```kotlin
val p2 = p1.copy(job = p1.job.copy(title = "Señor Developer"))

```

With KopyKat you can do this in a more readable way:

```kotlin
val p2 = p1.copy { job.title = "Señor Developer" }
```

> **Warning**
> For now, this doesn't work with types that are external to the source code (i.e. dependencies). We are working on
> supporting this in the future.

### Mapping `copyMap`

Instead of new *values*, `copyMap` takes as arguments the *transformations* that ought to be applied to each argument.

```kotlin
val p1 = Person("Alex", 1)
val p2 = p1.copyMap(age = { it + 1 })
```

> **Note**
> you can use `copyMap` to simulate `copy`, by making the transformation return a constant value.

```kotlin
val p3 = p1.copyMap(age = { 10 })
```

> **Note**
> When using value classes, given that you only have one property, you can skip the name of the property:

```kotlin
@JvmInline value class Age(ageValue: Int)

val a = Age(39)

val b = a.copyMap { it + 1 }
```

### `copy` for sealed hierarchies

KopyKat also works with sealed hierarchies. These are both sealed classes and sealed interfaces. It generates
regular `copy`, `copyMap`, and mutable `copy` for the common properties, which ought to be declared in the parent class.

```kotlin
abstract sealed class User(open val name: String)
data class Person(override val name: String, val age: Int): User(name)
data class Company(override val name: String, val address: String): User(name)
```

This means that the following code works directly, without requiring an intermediate `when`.

```kotlin
fun User.takeOver() = this.copy { name = "Me" }
```

Equally, you can use `copyMap` in a similar fashion:

```kotlin
fun User.takeOver() = this.copyMap(name = { "Me" })
```

Or, you can use a more familiar copy function:

```kotlin
fun User.takeOver() = this.copy(name = "Me")
```

> **Warning**
> KopyKat only generates these if all the subclasses are data or value classes. We can't mutate object types without
> breaking the world underneath them. And cause a lot of pain.

## Using KopyKat in your project

> This [demo project](https://github.com/kopykat-kt/kopykat-demo) showcases the use of KopyKat
> alongside [version catalogs](https://docs.gradle.org/7.0-rc-1/release-notes.html#centralized-versions).

KopyKat builds upon [KSP](https://kotlinlang.org/docs/ksp-overview.html), from which it inherits easy integration with
Gradle. To use this plug-in, add the following in your `build.gradle.kts`:

1. Add [Mvaen Central](https://search.maven.org/) to the list of repositories.

    ```kotlin
    repositories {
      mavenCentral()
    }
    ```

2. Add KSP to the list of plug-ins. You can check the latest version in their [releases](https://github.com/google/ksp/releases/).

    ```kotlin
    plugins {
      id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    }
    ```

3. Add a KSP dependency on KopyKat.

    ```kotlin
    dependencies {
      // other dependencies
      ksp("at.kopyk:kopykat-ksp:$kopyKatVersion")
    }
    ```

4. (Optional) If you are using IntelliJ as your IDE, we recommend you to [follow these steps](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code) to make it aware of the new code.

### Customizing the generation

You can disable the generation of some of these methods by [passing options to KSP](https://kotlinlang.org/docs/ksp-quickstart.html#pass-options-to-processors) in your Gradle file. For example, the following block disables the generation of `copyMap`.

```kotlin
ksp {
  arg("mutableCopy", "true")
  arg("copyMap", "false")
  arg("hierarchyCopy", "true")
}
```

By default, the three kinds of methods are generated.

## What about optics?

Optics, like the ones provided by [Arrow](https://arrow-kt.io/docs/optics/), are a much more powerful abstraction. Apart from changing fields, optics allow uniform access to collections, possibly-null values, and hierarchies of data classes. You can even define a [single `copy` function](https://github.com/arrow-kt/arrow/pull/2777) which works for _every_ type, instead of relying on generating an implementation for each data type.

KopyKat, on the other hand, aims to be just a tiny step further from Kotlin's built-in `copy`. By re-using well-known idioms, the barrier to introducing this plug-in becomes much lower. Our goal is to make it easier to work with immutable data classes.
