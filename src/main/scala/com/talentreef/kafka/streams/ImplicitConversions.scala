package com.talentreef.kafka.streams

import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.{Consumed, KeyValue}

import scala.annotation.implicitNotFound
import scala.language.implicitConversions

object ImplicitConversions{
  implicit def tupleToKeyValue[K, V](tuple: (K, V)): KeyValue[K, V] = KeyValue.pair(tuple._1, tuple._2)
}

/**
  * Type class for type pairs that can be used to create a `Serialized` instance.
  */
@implicitNotFound("Cannot create CanBeSerialized since no CanSerde[${K}] or CanSerde[${V}] found.")
trait CanBeSerialized[K, V]{
  def serialized(): Serialized[K, V]
}

object CanBeSerialized{
  implicit def apply[K, V](implicit canSerdeKey: CanSerde[K], canSerdeValue: CanSerde[V]): CanBeSerialized[K, V] = () => {
    Serialized.`with`(canSerdeKey.serde(), canSerdeValue.serde())
  }
}

/**
  * Type class for type pairs that can be used to create a `Produced` instance.
  */
@implicitNotFound("Cannot create CanBeSerialized since no CanSerde[${K}] or CanSerde[${V}] found.")
trait CanBeProduced[K, V]{
  def produced(): Produced[K, V]
}

object CanBeProduced{
  implicit def apply[K, V](implicit canSerdeKey: CanSerde[K], canSerdeValue: CanSerde[V]): CanBeProduced[K, V] = () => {
    Produced.`with`(canSerdeKey.serde(), canSerdeValue.serde())
  }
}

/**
  * Type class for type pairs that can be used to create a `Consumed` instance.
  */
@implicitNotFound("Cannot create CanBeSerialized since no CanSerde[${K}] or CanSerde[${V}] found.")
trait CanBeConsumed[K, V]{
  def consumed(): Consumed[K, V]
}

object CanBeConsumed{
  implicit def apply[K, V](implicit canSerdeKey: CanSerde[K], canSerdeValue: CanSerde[V]): CanBeConsumed[K, V] = () => {
    Consumed.`with`(canSerdeKey.serde(), canSerdeValue.serde())
  }
}

/**
  * Type class for triples that can be used to create a `Joined` instance.
  */
@implicitNotFound("Cannot create CanBeSerialized since no CanSerde[${K}] or CanSerde[${V}] or CanSerde[${V0}] found.")
trait CanBeJoined[K, V, V0]{
  def joined(): Joined[K, V, V0]
}

object CanBeJoined{
  implicit def apply[K, V, V0](implicit canSerdeKey: CanSerde[K], canSerdeValue: CanSerde[V], canSerdeNewValue: CanSerde[V0]):
  CanBeJoined[K, V, V0] = () => {
    Joined.`with`(canSerdeKey.serde(), canSerdeValue.serde(), canSerdeNewValue.serde())
  }
}
