# 😸 KopyKat

_When the author figures out how to upload the artifact to Maven, instructions on how to use the library will appear here._

- [The Three Methods](#the-three-methods)
  - [Mutable `copy`](#mutable-copy)
  - [Mapping `copyMap`](#mapping-copymap)
  - [Value class `copy`](#value-class-copy)
- [Using KopyKat in your project](#using-kopykat-in-your-project)
  - [Customizing the generation](#customizing-the-generation)
- [What about optics?](#what-about-optics)

One of the great features of Kotlin [data classes](https://kotlinlang.org/docs/data-classes.html) is their [`copy` method](https://kotlinlang.org/docs/data-classes.html#copying). But using it can become cumbersome very quickly, because you need to repeat the name of the field before and after.

```kotlin
data class Person(val name: String, val age: Int)

val p1 = Person("Alex", 1)
val p2 = p1.copy(age = p1.age + 1)  // too many 'age'!
```

## The Three Methods

This plug-in generates a couple of new methods that make working with immutable data classes much easier.

![IntelliJ showing the methods](https://github.com/serras/kopykat/blob/main/intellij.png?raw=true)

### Mutable `copy`

This new version of `copy` takes a *block* as parameter. Within that block mutability is simulated; the final assignment of each (mutable) variable becomes the value of the new copy.

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

### Mapping `copyMap`

Instead of new *values*, `copyMap` takes as arguments the *transformations* that ought to be applied to each argument.

```kotlin
val p1 = Person("Alex", 1)
val p2 = p1.copyMap(age = { it + 1 })
```

Note that you can use `copyMap` to simulate `copy`, by making the transformation return a constant value.

```kotlin
val p3 = p1.copyMap(age = { 10 })
```

### Value class `copy`

[Value-based classes](https://kotlinlang.org/docs/inline-classes.html) are useful to create wrapper that separate different concepts, but without any overhead. A good example is wrapping an integer as an age:

```kotlin
value class Age(val age: Int)
```

The plug-in generates a `copy` method which takes the transformation to apply to the _single_ field as parameter.

```kotlin
val a1 = Age(1)
val a2 = a1.copy { it + 1 }
```

## Using KopyKat in your project

> This [demo project](https://github.com/serras/kopykat-demo) showcases the use of KopyKat alongside [version catalogs](https://docs.gradle.org/7.0-rc-1/release-notes.html#centralized-versions).

KopyKat builds upon [KSP](https://kotlinlang.org/docs/ksp-overview.html), from which it inherits easy integration with Gradle. To use this plug-in, add the following in your `build.gradle.kts`:

1. Add [JitPack](https://jitpack.io/) to the list of repositories. 

    ```kotlin
    repositories {
      mavenCentral()
      maven(url = "https://jitpack.io")
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
      ksp("com.github.serras.kopykat:ksp:0.1")
    }
    ```

4. (Optional) If you are using IntelliJ as your IDE, we recommend you to [follow these steps](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code) to make it aware of the new code.

### Customizing the generation

You can disable the generation of some of these methods by [passing options to KSP](https://kotlinlang.org/docs/ksp-quickstart.html#pass-options-to-processors) in your Gradle file. For example, the following block disables the generation of `copyMap`.

```kotlin
ksp {
  arg("mutableCopy", "true")
  arg("copyMap", "false")
  arg("valueCopy", "true")
}
```

By default, the three kinds of methods are generated.

## What about optics?

Optics, like the ones provided by [Arrow](https://arrow-kt.io/docs/optics/), are a much more powerful abstraction. Apart from changing fields, optics allow uniform access to collections, possubly-null values, and hierarchies of data classes. You can even define a [single `copy` function](https://github.com/arrow-kt/arrow/pull/2777) which works for _every_ type, instead of relying on generating an implementation for each data type.

KopyKat, on the other hand, aims to be just a tiny step further from Kotlin's built-in `copy`. By re-using well-known idioms, the barrier to introducing this plug-in becomes much lower. Our goal is to make it easier to work with immutable data classes.
