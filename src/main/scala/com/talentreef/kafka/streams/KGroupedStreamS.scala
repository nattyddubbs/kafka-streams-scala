package com.talentreef.kafka.streams

import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.common.serialization.Serde
import FunctionConversions._


/**
 * Wraps the Java class KGroupedStream and delegates method calls to the underlying Java object.
 */
class KGroupedStreamS[K, V](inner: KGroupedStream[K, V]) {

  def count(): KTableS[K, Long] = {
    val c: KTableS[K, java.lang.Long] = new KTableS(inner.count())
    c.mapValues[Long](Long2long _)
  }

  def count(store: String, keySerde: Option[Serde[K]] = None): KTableS[K, Long] = {
    val materialized = keySerde.foldLeft(Materialized.as[K, java.lang.Long, KeyValueStore[Bytes, Array[Byte]]](store))((m,serde)=> m.withKeySerde(serde))

    val c =new KTableS(inner.count(materialized))
    c.mapValues[Long](Long2long _)
  }

  def reduce(reducer: (V, V) => V): KTableS[K, V] = {
    new KTableS(inner.reduce((v1, v2) => reducer(v1, v2)))
  }

  def reduce(reducer: (V, V) => V,
    materialized: Materialized[K, V, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, V] = {

    // need this explicit asReducer for Scala 2.11 or else the SAM conversion doesn't take place
    // works perfectly with Scala 2.12 though
    new KTableS(inner.reduce(((v1: V, v2: V) => reducer(v1, v2)).asReducer, materialized))
  }

  def reduce(reducer: (V, V) => V,
    storeName: String)(implicit canSerdeKey: CanSerde[K], canSerdeValue: CanSerde[V]): KTableS[K, V] = {

    // need this explicit asReducer for Scala 2.11 or else the SAM conversion doesn't take place
    // works perfectly with Scala 2.12 though
    new KTableS(inner.reduce(((v1: V, v2: V) =>
      reducer(v1, v2)).asReducer,
      Materialized.as[K, V, KeyValueStore[Bytes, Array[Byte]]](storeName)
        .withKeySerde(canSerdeKey.serde())
        .withValueSerde(canSerdeValue.serde())
    ))
  }

  def aggregate[VR](initializer: () => VR,
    aggregator: (K, V, VR) => VR): KTableS[K, VR] = {
    new KTableS(inner.aggregate(initializer.asInitializer, aggregator.asAggregator))
  }

  def aggregate[VR](initializer: () => VR,
    aggregator: (K, V, VR) => VR,
    materialized: Materialized[K, VR, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, VR] = {
    new KTableS(inner.aggregate(initializer.asInitializer, aggregator.asAggregator, materialized))
  }

  def windowedBy(windows: SessionWindows): SessionWindowedKStreamS[K, V] =
    new SessionWindowedKStreamS(inner.windowedBy(windows))

  def windowedBy[W <: Window](windows: Windows[W]): TimeWindowedKStreamS[K, V] =
    new TimeWindowedKStreamS(inner.windowedBy(windows))
}
