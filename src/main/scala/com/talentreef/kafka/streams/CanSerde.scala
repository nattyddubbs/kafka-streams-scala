package com.talentreef.kafka.streams

import org.apache.kafka.common.serialization.{Serde, Serdes}

import scala.annotation.implicitNotFound

/**
  * Type class for types that can a serde can be generated for.
  */
@implicitNotFound("No CanSerde[${T}] found for for type ${T}")
trait CanSerde[T]{
  def serde(): Serde[T]
}

object CanSerde{
  /** A `String` can be serialized and deserialized **/
  trait StringCanSerde extends CanSerde[String]{
    override val serde: Serde[String] = Serdes.String()
  }
  implicit object StringCanSerde extends StringCanSerde

  /** A `Long` can be serialized and deserialized **/
  trait LongCanSerde extends CanSerde[Long]{
    override val serde: Serde[Long] = Serdes.Long().asInstanceOf[Serde[Long]]
  }
  implicit object LongCanSerde extends LongCanSerde

  /** A `Double` can be serialized and deserialized **/
  trait DoubleCanSerde extends CanSerde[Double]{
    override val serde: Serde[Double] = Serdes.Double().asInstanceOf[Serde[Double]]
  }
  implicit object DoubleCanSerde extends DoubleCanSerde

  /** An `Array[Byte]` can be serialized and deserialized **/
  trait ByteArrayCanSerde extends CanSerde[Array[Byte]]{
    override val serde: Serde[Array[Byte]] = Serdes.ByteArray()
  }
  implicit object ByteArrayCanSerde extends ByteArrayCanSerde

  /** A `Float` can be serialized and deserialized **/
  trait FloatCanSerde extends CanSerde[Float]{
    override val serde: Serde[Float] = Serdes.Float().asInstanceOf[Serde[Float]]
  }
  implicit object FloatCanSerde extends FloatCanSerde

  /** An `Int` can be serialized and deserialized **/
  trait IntegerCanSerde extends CanSerde[Int]{
    override val serde: Serde[Int] = Serdes.Integer().asInstanceOf[Serde[Int]]
  }
  implicit object IntegerCanSerde extends IntegerCanSerde

  /** A `Bytes` can be serialized and deserialized **/
  trait KafkaBytesCanSerde extends CanSerde[org.apache.kafka.common.utils.Bytes]{
    override val serde = Serdes.Bytes()
  }
  implicit object KafkaBytesCanSerde extends KafkaBytesCanSerde
}
