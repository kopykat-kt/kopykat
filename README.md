<!-- TOC -->

* [What can KopyKat do?](#what-can-kopykat-do)
    * [Mutable `copy`](#mutable-copy)
        * [Nested mutation](#nested-mutation)
        * [Nested collections](#nested-collections)
    * [Mapping `copyMap`](#mapping-copymap)
    * [`copy` for sealed hierarchies](#copy-for-sealed-hierarchies)
    * [`copy` from supertypes](#copy-from-supertypes)
    * [`copy` for type aliases](#copy-for-type-aliases)
* [Isomorphic copy constructors](#isomorphic-copy-constructors)
    * [Nested Copy Constructors](#nested-copy-constructors)
* [Using KopyKat in your project](#using-kopykat-in-your-project)
    * [Enable only for selected types](#enable-only-for-selected-types)
        * [All classes in given packages](#all-classes-in-given-packages)
        * [Using annotations](#using-annotations)
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

<hr style="border-bottom: 3px dashed #b5e853;">

## What can KopyKat do?

This plug-in generates a couple of new methods that make working with immutable (read-only) types, like data classes and
value classes, more convenient.

![IntelliJ showing the methods](https://github.com/kopykat-kt/kopykat/blob/main/intellij.png?raw=true)

<hr>

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
data class Job(val title: String, val teams: List<String>)

val p1 = Person(name = "John", job = Job("Developer", listOf("Kotlin", "Training")))
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

#### Nested collections

The nested mutation also extends to collections, which are turned into their mutable counterparts, if they exist.

```kotlin
val p3 = p1.copy { job.teams.add("Compiler") }
```

To avoid unnecessary copies, we recommend to mutate the collections in-place as much as possible. This means that
`forEach` functions and mutation should be preferred over `map`.

```kotlin
val p4 = p1.copy { // needs an additional toMutableList at the end
  job.teams = job.teams.map { it.capitalize() }.toMutableList()
}
val p5 = p1.copy { // mutates the job.teams collection in-place
  job.teams.forEachIndexed { i, team -> job.teams[i] = team.capitalize() }
}
```

The `at.kopyk:mutable-utils` library ([documentation](https://kopyk.at/docs/mutable-utils/mutable-utils/at.kopyk/index.html)) contains versions of the main collection functions which reuse the same structure.

```kotlin
val p6 = p1.copy { // mutates the job.teams collection in-place
  job.teams.mutateAll { it.capitalize() }
}
```

<hr>

### Mapping `copyMap`

Instead of new *values*, `copyMap` takes as arguments the *transformations* that ought to be applied to each argument.
The "old" value of each field is given as argument to each of the functions, so you can refer to it using `it` or
introduce an explicit name.

```kotlin
val p1 = Person("Alex", 1)
val p2 = p1.copyMap(age = { it + 1 })
val p3 = p1.copyMap(name = { nm -> nm.capitalize() })
```

The whole "old" value (the `Person` in the example above) is given as receiver to each of the transformations. That
means that you can access all the other fields in the body of each of the transformations.

```kotlin
val p4 = p1.copyMap(age = { name.count() })
```

> **Note**
> you can use `copyMap` to simulate `copy`, by making the transformation return a constant value.

```kotlin
val p5 = p1.copyMap(age = { 10 })
```

> **Note**
> When using value classes, given that you only have one property, you can skip the name of the property.

```kotlin
@JvmInline value class Age(ageValue: Int)

val a = Age(39)

val b = a.copyMap { it + 1 }
```

<hr>

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

<hr>

## Isomorphic copy constructors

We know, isomorphic seems like a big word. However, it just means that two things are similar
in some way. In this case KopyKat can generate copy constructors between two types that
have the same properties, with the same name, and the same type.

In Kotlin, a copy constructor is a top level function with the same name as the type (in PascalCase)
that returns the given type. This naming pattern is described in the official (Kotlin Code
Conventions)[https://kotlinlang.org/docs/coding-conventions.html#function-names].

To generate these you have to annotate your types with one of the following:

- `@Copy`
- `@CopyFrom`
- `@CopyTo`

All of them take another type to copy from/to, as a parameter. In the case of `@Copy`, it will
generate two functions for both directions. So, if we have code like this:

```kotlin
data class Person(val name: String, val age: Int)

@Copy(Person::class)
data class LocalPerson(val name: String, val age: Int)
```

The following code is generated:

```kotlin
inline fun Person(from: LocalPerson): Person =
    Person(name = from.name, age = from.age)

inline fun LocalPerson(from: Person): Person =
    LocalPerson(name = from.name, age = from.age)
```

These allow to convert from one type to the other and vice versa. This is quite a common pattern used to
cross boundaries of the different layers of an application. Often, they are called mappers.

If you don't need either of the copy constructors, you can use either `@CopyFrom` or `@CopyTo`. `@CopyFrom` will
generate a copy constructor from the provided type to the annotated type (`LocalPerson -> Person`). On the other hand,
if you use `@CopyTo` will generate the oposite (`Person -> LocalPerson`).

### Nested Copy Constructors

In some cases you may want to have properties that are different on both types. To support data trees like that, you
must make sure that they have copy constructors generated as well. For example:

```kotlin
data class Person(val name: String, val job: Job)
data class Job(val title: String)

@Copy(Person::class) data class LocalPerson(val name: String, val job: LocalJob)
@Copy(Job::class) data class LocalJob(val title: String)

val localPerson = LocalPerson("Alice", LocalJob("Developer"))
val person = Person(localPerson)
check(person.name == "Alice")
check(person.job.title == "Developer")
```

In this example, if `LocalJob` is not annotated wih `@Copy` (or `@CopyFrom`) the compiler will complain about it.

### Multiple Copy Constructors

Annotations for copy constructors (`@Copy[From|To]`) can be applied more than once to the same type. This means
that you can define mapping across multiple isomorphic types:

```kotlin
@Copy(LocalPerson::class)
@Copy(RemotePerson::class)
data class Person(val name: String, val age: Int)

data class LocalPerson(val name: String, val age: Int)

data class RemotePerson(val name: String, val age: Int)
```

This configuration will generate 4 different copy constructors.

<hr>

### `copy` for type aliases

KopyKat can also generate the different `copy` methods for a type alias.

```kotlin
@CopyExtensions
typealias Person = Pair<String, Int>

// generates the following methods
fun Person.copyMap(first: (String) -> String, second: (Int) -> Int): Person = TODO()
fun Person.copy(block: `Person$Mutable`.() -> Unit): Person = TODO()
```

The following must hold for the type alias to be processed:
- It must be marked with the `@CopyExtensions` annotation,
- It must refer to a data or value class, or a type hierarchy of those.

<hr style="border-bottom: 3px dashed #b5e853;">

## Using KopyKat in your project

> This [demo project](https://github.com/kopykat-kt/kopykat-demo) showcases the use of KopyKat
> alongside [version catalogs](https://docs.gradle.org/7.0-rc-1/release-notes.html#centralized-versions).

KopyKat builds upon [KSP](https://kotlinlang.org/docs/ksp-overview.html), from which it inherits easy integration with
Gradle. To use this plug-in, add the following in your `build.gradle.kts`:

1. Add [Maven Central](https://search.maven.org/) to the list of repositories.

    ```kotlin
    repositories {
      mavenCentral()
    }
    ```

2. Add KSP to the list of plug-ins. You can check the latest version in their
   [releases](https://github.com/google/ksp/releases/).

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

4. (Optional) If you are using IntelliJ as your IDE, we recommend you to 
   [follow these steps](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code) to make it 
   aware of the new code.

### Enable only for selected types

By default, KopyKat generates methods for **every** data and value class, and sealed hierarchies of those. If you prefer
to enable generation for only some classes, this is of course possible. Note that you always require a `@CopyExtensions`
annotation to process a type alias.

#### All classes in given packages

Change the `generate` option for the plug-in, by
[passing options to KSP](https://kotlinlang.org/docs/ksp-quickstart.html#pass-options-to-processors).
The packages should be separated by `:`, and you can use wildcards, as supported by
[`wildcardMatch`](https://commons.apache.org/proper/commons-io/javadocs/api-release/org/apache/commons/io/FilenameUtils.html#wildcardMatch-java.lang.String-java.lang.String-).

```kotlin
ksp {
  arg("generate", "packages:my.example.*")
}
```

#### Using annotations

1. Add a dependency to KopyKat's annotation package. Note that we declare it as `compileOnly`, which means there's no
   trace of it in the compiled artifact.
 
    ```kotlin
    dependencies {
      // other dependencies
      compileOnly("at.kopyk:kopykat-annotations:$kopyKatVersion")
    }
    ```

2. Change the `generate` option for the plug-in, by
   [passing options to KSP](https://kotlinlang.org/docs/ksp-quickstart.html#pass-options-to-processors).

    ```kotlin
    ksp {
      arg("generate", "annotated")
    }
    ```

3. Mark those classes you want KopyKat to process with the `@CopyExtensions` annotation.

    ```kotlin
    import at.kopyk.CopyExtensions
    
    @CopyExtensions data class Person(val name: String, val age: Int)
    ```

### Customizing the generation

You can disable the generation of some of these methods by 
[passing options to KSP](https://kotlinlang.org/docs/ksp-quickstart.html#pass-options-to-processors) in your Gradle
file. For example, the following block disables the generation of `copyMap`.

```kotlin
ksp {
  arg("mutableCopy", "true")
  arg("copyMap", "false")
  arg("hierarchyCopy", "true")
  arg("superCopy", "true")
}
```

By default, the three kinds of methods are generated.

<hr style="border-bottom: 3px dashed #b5e853;">

## What about optics?

Optics, like the ones provided by [Arrow](https://arrow-kt.io/docs/optics/), are a much more powerful abstraction. Apart
from changing fields, optics allow uniform access to collections, possibly-null values, and hierarchies of data classes.
You can even define a [single `copy` function](https://github.com/arrow-kt/arrow/pull/2777) which works for _every_ 
type, instead of relying on generating an implementation for each data type.

KopyKat, on the other hand, aims to be just a tiny step further from Kotlin's built-in `copy`. By re-using well-known 
idioms, the barrier to introducing this plug-in becomes much lower. Our goal is to make it easier to work with immutable
data classes.
