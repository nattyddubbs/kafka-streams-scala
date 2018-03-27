# A Thin Scala Wrapper Around the Kafka Streams Java API

[![Build Status](https://secure.travis-ci.org/nattyddubbs/kafka-streams-scala.png)](http://travis-ci.org/nattyddubbs/kafka-streams-scala)

The library wraps Java APIs in Scala thereby providing:

1. much better type inference in Scala
2. less boilerplate in application code
3. the usual builder-style composition that developers get with the original Java API
4. complete compile time type safety

The design of the library was inspired by the typesafe library in [this repository](https://github.com/typesafe/kafka-streams-scala).
While Lightbend chose to use implicit conversions this library leans more on type classes so as to avoid explicit imports. 

## Quick Start

`kafka-streams-scala` is published and cross-built for Scala `2.11`, and `2.12`, so you can just add the following to your build:

```scala
val kafka_streams_scala_version = "0.2.0"

libraryDependencies ++= Seq("com.talentreef" %%
  "kafka-streams-scala" % kafka_streams_scala_version)
```

> Note: `kafka-streams-scala` supports onwards Kafka Streams `1.0.0`.

## Running the Tests

The library comes with an embedded Kafka server. To run the tests, simply run `sbt testOnly` and all tests will run on the local embedded server.

## Type Inference and Composition

Here's a sample code fragment using the Scala wrapper library. Compare this with the Scala code from the same [example](https://github.com/confluentinc/kafka-streams-examples/blob/4.0.0-post/src/test/scala/io/confluent/examples/streams/StreamToTableJoinScalaIntegrationTest.scala) in Confluent's repository.

```scala
// Compute the total per region by summing the individual click counts per region.
val clicksPerRegion: KTableS[String, Long] = userClicksStream

  // Join the stream against the table.
  .leftJoin(userRegionsTable, (clicks: Long, region: String) => (if (region == null) "UNKNOWN" else region, clicks))

  // Change the stream from <user> -> <region, clicks> to <region> -> <clicks>
  .map((_, regionWithClicks) => regionWithClicks)

  // Compute the total per region by summing the individual click counts per region.
  .groupByKey
  .reduce(_ + _)
```

## Implicit Serdes

One of the areas where the Java APIs' verbosity can be reduced is through a succinct way to pass serializers and de-serializers to the various functions. The library uses the power of Scala implicits towards this end. The library makes some decisions that help implement more succinct serdes in a type safe manner:

1. No use of configuration based default serdes. Java APIs allow the user to define default key and value serdes as part of the configuration. This configuration, being implemented as `java.util.Properties` is type-unsafe and hence can result in runtime errors in case the user misses any of the serdes to be specified or plugs in an incorrect serde. `kafka-streams-scala` makes this completely type-safe by allowing all serdes to be specified through Scala implicits.
2. The libraty offers implicit conversions to `Serialized`, `Produced`, `Consumed` or `Joined`. Hence as a user you just have to pass in the implicit `CanSerde` and all conversions to `Serialized`, `Produced`, `Consumed` or `Joined` will be taken care of automatically.


### Default Serdes

The library offers a `CanSerde` type class that has been implemented for all out of the box `Serde` implementations. If you have an additional type that you would like to serialize then implement a `CanSerde[T]` for that type in implicit scope of your streams.

Since all builder APIs accept the `CanSerde` type class there are no explicit imports required.

```scala
/**
  * Type class for types that can a serde can be generated for.
  */
trait CanSerde[T]{
  def serde(): Serde[T]
}

object CanSerde{
  /** A `String` can be serialized and deserialized **/
  trait StringCanSerde extends CanSerde[String]{
    override val serde: Serde[String] = Serdes.String()
  }
  implicit object StringCanSerde extends StringCanSerde
  
  // other out of the box CanSerde instances live here.
}


// Add your own implementation
object MyTypeCanSerde extends CanSerde[MyType]{
  def serde(): Serde[MyType] = ???
}
```

### Compile time typesafety

```scala
val clicksPerRegion: KTableS[String, Long] =
  userClicksStream

  // Join the stream against the table.
  .leftJoin(userRegionsTable, (clicks: Long, region: String) => (if (region == null) "UNKNOWN" else region, clicks))

  // Change the stream from <user> -> <region, clicks> to <region> -> <clicks>
  .map((_, regionWithClicks) => regionWithClicks)

  // Compute the total per region by summing the individual click counts per region.
  .groupByKey
  .reduce(_ + _)

  // Write the (continuously updating) results to the output topic.
  clicksPerRegion.toStream.to(outputTopic)
```
